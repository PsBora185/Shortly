package com.urlShortner.service;

import com.urlShortner.dto.AuthResponse;
import com.urlShortner.dto.LoginRequest;
import com.urlShortner.dto.RegisterRequest;
import com.urlShortner.entity.AuthProvider;
import com.urlShortner.entity.OtpSession;
import com.urlShortner.entity.User;
import com.urlShortner.entity.UserRole;
import com.urlShortner.exception.BadRequestException;
import com.urlShortner.exception.ConflictException;
import com.urlShortner.exception.ResourceNotFoundException;
import com.urlShortner.repository.OtpSessionRepository;
import com.urlShortner.repository.UserRepository;
import com.urlShortner.security.JwtService;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final OtpSessionRepository otpSessionRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final EmailService emailService;
	private final SecureRandom secureRandom = new SecureRandom();

	public AuthService(UserRepository userRepository, OtpSessionRepository otpSessionRepository,
			PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService) {
		this.userRepository = userRepository;
		this.otpSessionRepository = otpSessionRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.emailService = emailService;
	}

	@Transactional
	public void sendOtp(String rawEmail) {
		String email = normalizeEmail(rawEmail);
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email already registered");
		}
		generateAndSendOtp(email);
	}

	@Transactional
	public void sendForgotPasswordOtp(String rawEmail) {
		String email = normalizeEmail(rawEmail);
		if (!userRepository.existsByEmail(email)) {
			throw new ResourceNotFoundException("User not found");
		}
		generateAndSendOtp(email);
	}

	private void generateAndSendOtp(String email) {
		// If a recent OTP was sent within the last 60s, don't generate a new one
		otpSessionRepository.findFirstByEmailOrderByCreatedAtDesc(email).ifPresent(existing -> {
			if (existing.getCreatedAt().isAfter(Instant.now().minusSeconds(60))) {
				throw new BadRequestException("Please wait before requesting another OTP");
			}
			otpSessionRepository.delete(existing);
		});

		String otp = String.format("%06d", secureRandom.nextInt(1000000));
		OtpSession session = new OtpSession();
		session.setEmail(email);
		session.setOtpCode(otp);
		session.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));
		session.setVerified(false);
		session.setCreatedAt(Instant.now());
		otpSessionRepository.save(session);

		emailService.sendOtpEmail(email, otp);
	}

	@Transactional
	public void verifyOtp(String rawEmail, String otp) {
		String email = normalizeEmail(rawEmail);
		OtpSession session = otpSessionRepository.findFirstByEmailOrderByCreatedAtDesc(email)
				.orElseThrow(() -> new BadRequestException("No pending OTP request found"));

		if (session.getExpiresAt().isBefore(Instant.now())) {
			otpSessionRepository.delete(session);
			throw new BadRequestException("OTP has expired");
		}

		if (!session.getOtpCode().equals(otp)) {
			throw new BadRequestException("Invalid OTP code");
		}

		session.setVerified(true);
		otpSessionRepository.save(session);
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email already registered");
		}
		OtpSession session = otpSessionRepository.findFirstByEmailOrderByCreatedAtDesc(email)
				.orElseThrow(() -> new BadRequestException("Please verify your email first"));
		if (!session.isVerified()) {
			throw new BadRequestException("Email not verified");
		}

		User user = new User();
		user.setFullName(request.fullName().trim());
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setProvider(AuthProvider.LOCAL);
		user.setRole(UserRole.USER);
		user.setEnabled(true);
		user.setCreatedAt(Instant.now());
		user = userRepository.save(user);

		otpSessionRepository.delete(session);
		return issueToken(user);
	}

	@Transactional
	public void resetPassword(String rawEmail, String newPassword) {

		String email = normalizeEmail(rawEmail);
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		OtpSession session = otpSessionRepository.findFirstByEmailOrderByCreatedAtDesc(email)
				.orElseThrow(() -> new BadRequestException("Please verify your email first"));

		if (!session.isVerified()) {
			throw new BadRequestException("Email not verified");
		}

		user.setPasswordHash(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		otpSessionRepository.delete(session);
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(normalizeEmail(request.email()))
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
		if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new BadCredentialsException("Invalid email or password");
		}

		user.setLastLoginAt(Instant.now());
		userRepository.save(user);
		return issueToken(user);
	}

	@Transactional
	public User seedAdmin(String email, String password, String fullName) {
		String normalizedEmail = normalizeEmail(email);
		User user = userRepository.findByEmail(normalizedEmail).orElseGet(User::new);
		user.setEmail(normalizedEmail);
		user.setFullName(fullName == null || fullName.isBlank() ? "Administrator" : fullName.trim());
		user.setPasswordHash(passwordEncoder.encode(password));
		user.setProvider(AuthProvider.LOCAL);
		user.setProviderId(null);
		user.setRole(UserRole.ADMIN);
		user.setEnabled(true);
		if (user.getCreatedAt() == null) {
			user.setCreatedAt(Instant.now());
		}
		return userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public User getByEmail(String email) {
		return userRepository.findByEmail(normalizeEmail(email))
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	public AuthResponse issueToken(User user) {
		JwtService.AuthToken token = jwtService.issueToken(user);
		return new AuthResponse(
				token.token(),
				"Bearer",
				token.expiresInSeconds(),
				token.expiresAt(),
				user.getEmail(),
				user.getFullName(),
				user.getRole().name(),
				user.getProvider().name());
	}

	private String normalizeEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase();
	}
}
