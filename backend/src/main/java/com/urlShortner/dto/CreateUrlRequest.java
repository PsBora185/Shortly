package com.urlShortner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateUrlRequest(
		@NotBlank(message = "Original URL is required")
		@Size(max = 5000, message = "Original URL is too long")
		String originalUrl,

		@Pattern(regexp = "^[A-Za-z0-9_-]{4,32}$", message = "Custom short code must be 4 to 32 characters and use letters, numbers, hyphen or underscore")
		String customShortCode,

		Instant expiresAt) {
}
