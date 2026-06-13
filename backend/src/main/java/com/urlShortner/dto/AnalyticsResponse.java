package com.urlShortner.dto;

import java.time.Instant;

public record AnalyticsResponse(
		String shortCode,
		long clicks,
		Instant createdAt,
		Instant lastAccessed,
		Instant expiresAt) {
}
