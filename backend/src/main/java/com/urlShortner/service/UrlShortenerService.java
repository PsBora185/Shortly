package com.urlShortner.service;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urlShortner.dto.AnalyticsResponse;
import com.urlShortner.dto.CreateUrlRequest;
import com.urlShortner.dto.UrlResponse;
import com.urlShortner.entity.UrlAnalyticsEntity;
import com.urlShortner.entity.UrlMappingEntity;
import com.urlShortner.exception.BadRequestException;
import com.urlShortner.exception.ConflictException;
import com.urlShortner.exception.GoneException;
import com.urlShortner.exception.ResourceNotFoundException;
import com.urlShortner.repository.UrlAnalyticsRepository;
import com.urlShortner.repository.UrlRepository;

@Service
public class UrlShortenerService {

	private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final int GENERATED_CODE_LENGTH = 8;

	private final UrlRepository urlRepository;
	private final UrlAnalyticsRepository analyticsRepository;
	private final String publicBaseUrl;

	public UrlShortenerService(
			UrlRepository urlRepository,
			UrlAnalyticsRepository analyticsRepository,
			@Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
		this.urlRepository = urlRepository;
		this.analyticsRepository = analyticsRepository;
		this.publicBaseUrl = normalizeBaseUrl(publicBaseUrl);
	}

	@Transactional
	public UrlResponse create(CreateUrlRequest request, String ownerEmail) {
		String originalUrl = normalizeAndValidateUrl(request.originalUrl());
		String shortCode = resolveAvailableShortCode(request.customShortCode());
		Instant now = Instant.now();
		Instant expiresAt = request.expiresAt();
		if (expiresAt != null && expiresAt.isBefore(now)) {
			throw new BadRequestException("Expiration time must be in the future");
		}

		UrlMappingEntity url = new UrlMappingEntity();
		url.setOriginalUrl(originalUrl);
		url.setShortCode(shortCode);
		url.setOwnerEmail(normalizeOwnerEmail(ownerEmail));
		url.setCreatedAt(now);
		url.setExpiresAt(expiresAt);
		urlRepository.save(url);

		UrlAnalyticsEntity analytics = new UrlAnalyticsEntity();
		analytics.setShortCode(shortCode);
		analytics.setClicks(0L);
		analyticsRepository.save(analytics);

		return toResponse(url, analytics);
	}

	@Transactional(readOnly = true)
	public List<UrlResponse> listAll(String ownerEmail, boolean admin) {
		List<UrlMappingEntity> urls = admin
				? urlRepository.findAllByOrderByCreatedAtDesc()
				: urlRepository.findAllByOwnerEmailOrderByCreatedAtDesc(normalizeOwnerEmail(ownerEmail));
		return toResponses(urls, loadAnalytics(urls));
	}

	@Transactional(readOnly = true)
	public List<UrlResponse> listExpired(String ownerEmail, boolean admin) {
		Instant now = Instant.now();
		List<UrlMappingEntity> urls = admin
				? urlRepository.findByExpiresAtLessThanEqual(now)
				: urlRepository.findByOwnerEmailAndExpiresAtLessThanEqual(normalizeOwnerEmail(ownerEmail), now);
		return toResponses(urls, loadAnalytics(urls));
	}

	@Transactional
	public long purgeExpiredUrls() {
		Instant now = Instant.now();
		List<UrlMappingEntity> expired = urlRepository.findByExpiresAtLessThanEqual(now);
		if (expired.isEmpty()) {
			return 0L;
		}

		List<String> shortCodes = expired.stream().map(UrlMappingEntity::getShortCode).toList();
		analyticsRepository.deleteByShortCodeIn(shortCodes);
		urlRepository.deleteAll(expired);
		return expired.size();
	}

	@Transactional
	public void deleteById(UUID id, String ownerEmail, boolean admin) {
		UrlMappingEntity url;
		if (admin) {
			url = urlRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("URL not found: " + id));
		} else {
			url = urlRepository.findByIdAndOwnerEmail(id, normalizeOwnerEmail(ownerEmail))
					.orElseThrow(() -> new ResourceNotFoundException("URL not found: " + id));
		}
		analyticsRepository.deleteByShortCode(url.getShortCode());
		urlRepository.delete(url);
	}

	@Transactional(readOnly = true)
	public AnalyticsResponse getAnalytics(String shortCode, String ownerEmail, boolean admin) {
		UrlMappingEntity url = admin ? findActiveOrExpiredUrl(shortCode) : findOwnedUrl(shortCode, ownerEmail);
		UrlAnalyticsEntity analytics = analyticsRepository.findByShortCode(url.getShortCode())
				.orElseThrow(() -> new ResourceNotFoundException("Analytics not found for short code: " + shortCode));
		return new AnalyticsResponse(
				url.getShortCode(),
				analytics.getClicks(),
				url.getCreatedAt(),
				analytics.getLastAccessed(),
				url.getExpiresAt());
	}

	@Transactional
	public UrlMappingEntity resolveUrlForRedirect(String shortCode) {
		UrlMappingEntity url = findActiveOrExpiredUrl(shortCode);
		if (url.isExpired(Instant.now())) {
			throw new GoneException("This short URL has expired");
		}

		UrlAnalyticsEntity analytics = analyticsRepository.findByShortCode(shortCode)
				.orElseThrow(() -> new ResourceNotFoundException("Analytics not found for short code: " + shortCode));
		analytics.registerClick(Instant.now());
		analyticsRepository.save(analytics);
		return url;
	}

	private UrlMappingEntity findActiveOrExpiredUrl(String shortCode) {
		String normalizedShortCode = normalizeShortCode(shortCode);
		return urlRepository.findByShortCode(normalizedShortCode)
				.orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode));
	}

	private UrlMappingEntity findOwnedUrl(String shortCode, String ownerEmail) {
		String normalizedShortCode = normalizeShortCode(shortCode);
		return urlRepository.findByShortCodeAndOwnerEmail(normalizedShortCode, normalizeOwnerEmail(ownerEmail))
				.orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode));
	}

	private List<UrlResponse> toResponses(List<UrlMappingEntity> urls, Map<String, UrlAnalyticsEntity> analyticsMap) {
		List<UrlResponse> responses = new ArrayList<>(urls.size());
		for (UrlMappingEntity url : urls) {
			UrlAnalyticsEntity analytics = analyticsMap.get(url.getShortCode());
			responses.add(toResponse(url, analytics));
		}
		return responses;
	}

	private Map<String, UrlAnalyticsEntity> loadAnalytics(Collection<UrlMappingEntity> urls) {
		List<String> shortCodes = urls.stream()
				.map(UrlMappingEntity::getShortCode)
				.filter(Objects::nonNull)
				.toList();
		if (shortCodes.isEmpty()) {
			return Map.of();
		}
		return analyticsRepository.findAllByShortCodeIn(shortCodes).stream()
				.collect(Collectors.toMap(UrlAnalyticsEntity::getShortCode, Function.identity()));
	}

	private UrlResponse toResponse(UrlMappingEntity url, UrlAnalyticsEntity analytics) {
		long clicks = analytics != null ? analytics.getClicks() : 0L;
		Instant lastAccessed = analytics != null ? analytics.getLastAccessed() : null;
		return new UrlResponse(
				url.getId(),
				url.getOriginalUrl(),
				url.getShortCode(),
				buildShortUrl(url.getShortCode()),
				url.getCreatedAt(),
				url.getExpiresAt(),
				clicks,
				lastAccessed);
	}

	private String buildShortUrl(String shortCode) {
		return publicBaseUrl + "/" + shortCode;
	}

	private String normalizeBaseUrl(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:8080";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	private String resolveAvailableShortCode(String requestedShortCode) {
		if (requestedShortCode != null && !requestedShortCode.isBlank()) {
			String normalizedShortCode = normalizeShortCode(requestedShortCode);
			if (urlRepository.findByShortCode(normalizedShortCode).isPresent()) {
				throw new ConflictException("Short code already exists: " + normalizedShortCode);
			}
			return normalizedShortCode;
		}

		for (int attempt = 0; attempt < 20; attempt++) {
			String generated = generateShortCode();
			if (urlRepository.findByShortCode(generated).isEmpty()) {
				return generated;
			}
		}

		throw new ConflictException("Unable to generate a unique short code");
	}

	private String normalizeShortCode(String shortCode) {
		String value = Optional.ofNullable(shortCode).orElseThrow(() -> new BadRequestException("Short code is required"))
				.trim();
		if (!value.matches("^[A-Za-z0-9_-]{4,32}$")) {
			throw new BadRequestException("Short code must be 4 to 32 characters and use letters, numbers, hyphen or underscore");
		}
		return value;
	}

	private String normalizeAndValidateUrl(String originalUrl) {
		String value = Optional.ofNullable(originalUrl).orElseThrow(() -> new BadRequestException("Original URL is required"))
				.trim();
		try {
			URI uri = URI.create(value);
			if (uri.getScheme() == null || uri.getHost() == null) {
				throw new BadRequestException("Original URL must include a valid scheme and host");
			}
			if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
				throw new BadRequestException("Original URL must use http or https");
			}
			return value;
		} catch (IllegalArgumentException exception) {
			throw new BadRequestException("Original URL is invalid");
		}
	}

	private String normalizeOwnerEmail(String ownerEmail) {
		return Optional.ofNullable(ownerEmail)
				.orElseThrow(() -> new BadRequestException("Authenticated user email is required"))
				.trim()
				.toLowerCase();
	}

	private String generateShortCode() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		StringBuilder builder = new StringBuilder(GENERATED_CODE_LENGTH);
		for (int i = 0; i < GENERATED_CODE_LENGTH; i++) {
			builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
		}
		return builder.toString();
	}
}
