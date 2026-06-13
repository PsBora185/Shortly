package com.urlShortner.service;

import com.urlShortner.dto.AuthResponse;
import com.urlShortner.dto.LoginRequest;
import com.urlShortner.dto.RegisterRequest;
import com.urlShortner.entity.AppUser;
import com.urlShortner.entity.AuthProvider;
import com.urlShortner.entity.UserRole;
import com.urlShortner.exception.ConflictException;
import com.urlShortner.exception.ResourceNotFoundException;
import com.urlShortner.repository.UserRepository;
import com.urlShortner.security.JwtService;
import java.time.Instant;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email already registered");
		}

		AppUser user = new AppUser();
		user.setFullName(request.fullName().trim());
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setProvider(AuthProvider.LOCAL);
		user.setRole(UserRole.USER);
		user.setEnabled(true);
		user.setCreatedAt(Instant.now());
		user = userRepository.save(user);
		return issueToken(user);
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		AppUser user = userRepository.findByEmail(normalizeEmail(request.email()))
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
		if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new BadCredentialsException("Invalid email or password");
		}

		user.setLastLoginAt(Instant.now());
		userRepository.save(user);
		return issueToken(user);
	}

	@Transactional
	public AppUser upsertGoogleUser(String email, String fullName, String providerId) {
		String normalizedEmail = normalizeEmail(email);
		if (normalizedEmail.isBlank()) {
			throw new IllegalArgumentException("Google account did not provide an email address");
		}
		AppUser user = userRepository.findByEmail(normalizedEmail).orElseGet(AppUser::new);
		user.setEmail(normalizedEmail);
		user.setFullName(fullName == null || fullName.isBlank() ? normalizedEmail : fullName.trim());
		user.setProvider(AuthProvider.GOOGLE);
		user.setProviderId(providerId);
		user.setRole(user.getRole() == null ? UserRole.USER : user.getRole());
		user.setEnabled(true);
		if (user.getCreatedAt() == null) {
			user.setCreatedAt(Instant.now());
		}
		user.setLastLoginAt(Instant.now());
		return userRepository.save(user);
	}

	@Transactional
	public AppUser seedAdmin(String email, String password, String fullName) {
		String normalizedEmail = normalizeEmail(email);
		AppUser user = userRepository.findByEmail(normalizedEmail).orElseGet(AppUser::new);
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
	public AppUser getByEmail(String email) {
		return userRepository.findByEmail(normalizeEmail(email))
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	public AuthResponse issueToken(AppUser user) {
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
