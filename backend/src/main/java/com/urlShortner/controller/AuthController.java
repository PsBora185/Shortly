package com.urlShortner.controller;

import com.urlShortner.dto.AuthResponse;
import com.urlShortner.dto.LoginRequest;
import com.urlShortner.dto.MessageResponse;
import com.urlShortner.dto.RegisterRequest;
import com.urlShortner.dto.SendOtpRequest;
import com.urlShortner.dto.VerifyOtpRequest;
import com.urlShortner.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/send-otp")
	public ResponseEntity<MessageResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
		authService.sendOtp(request.email());
		return ResponseEntity.ok(new MessageResponse("OTP sent successfully"));
	}

	@PostMapping("/forgot-password-otp")
	public ResponseEntity<MessageResponse> sendForgotPasswordOtp(@Valid @RequestBody SendOtpRequest request) {
		authService.sendForgotPasswordOtp(request.email());
		return ResponseEntity.ok(new MessageResponse("OTP sent successfully"));
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<MessageResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
		authService.verifyOtp(request.email(), request.otp());
		return ResponseEntity.ok(new MessageResponse("OTP verified successfully"));
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody RegisterRequest request) {
		authService.resetPassword(request.email(), request.password());
		return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}
}
