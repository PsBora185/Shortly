package com.urlShortner.dto;

import java.time.Instant;
import java.util.UUID;

public record UrlResponse(
		UUID id,
		String originalUrl,
		String shortCode,
		String shortUrl,
		Instant createdAt,
		Instant expiresAt,
		long clicks,
		Instant lastAccessed) {
}
