package com.urlShortner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.urlShortner.dto.LoginRequest;
import com.urlShortner.dto.RegisterRequest;
import com.urlShortner.entity.User;
import com.urlShortner.entity.AuthProvider;
import com.urlShortner.entity.OtpSession;
import com.urlShortner.entity.UserRole;
import com.urlShortner.exception.BadRequestException;
import com.urlShortner.repository.OtpSessionRepository;
import com.urlShortner.repository.UserRepository;
import com.urlShortner.security.JwtService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private OtpSessionRepository otpSessionRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private EmailService emailService;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(userRepository, otpSessionRepository, passwordEncoder, jwtService, emailService);
	}

	@Test
	void registerCreatesLocalUserWhenVerified() {
		when(userRepository.existsByEmail("user@example.com")).thenReturn(false);

		OtpSession session = new OtpSession();
		session.setEmail("user@example.com");
		session.setVerified(true);
		when(otpSessionRepository.findFirstByEmailOrderByCreatedAtDesc("user@example.com"))
				.thenReturn(Optional.of(session));

		when(passwordEncoder.encode("password123")).thenReturn("hashed");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(UUID.randomUUID());
			return user;
		});
		when(jwtService.issueToken(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			return new JwtService.AuthToken("token", Instant.now().plusSeconds(10), 10);
		});

		assertThat(authService.register(new RegisterRequest("Test User", "user@example.com", "password123")).email())
				.isEqualTo("user@example.com");
	}

	@Test
	void registerFailsWhenNotVerified() {
		when(userRepository.existsByEmail("unverified@example.com")).thenReturn(false);
		when(otpSessionRepository.findFirstByEmailOrderByCreatedAtDesc("unverified@example.com"))
				.thenReturn(Optional.empty());

		assertThatThrownBy(
				() -> authService.register(new RegisterRequest("Test User", "unverified@example.com", "password123")))
				.isInstanceOf(BadRequestException.class);
	}

	@Test
	void loginRejectsInvalidPassword() {
		User user = new User();
		user.setEmail("user@example.com");
		user.setPasswordHash("hashed");
		user.setRole(UserRole.USER);
		user.setProvider(AuthProvider.LOCAL);

		when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrongpassword", "hashed")).thenReturn(false);

		assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "wrongpassword")))
				.isInstanceOf(BadCredentialsException.class);
	}
}
