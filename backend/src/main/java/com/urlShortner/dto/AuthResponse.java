package com.urlShortner.dto;

import java.time.Instant;

public record AuthResponse(
		String token,
		String tokenType,
		long expiresInSeconds,
		Instant expiresAt,
		String email,
		String fullName,
		String role,
		String provider) {
}
