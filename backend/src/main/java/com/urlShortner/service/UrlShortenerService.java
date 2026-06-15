package com.urlShortner.service;

import com.urlShortner.dto.AnalyticsResponse;
import com.urlShortner.dto.CreateUrlRequest;
import com.urlShortner.dto.UpdateUrlRequest;
import com.urlShortner.dto.UrlResponse;
import com.urlShortner.entity.Url;
import com.urlShortner.entity.User;
import com.urlShortner.exception.BadRequestException;
import com.urlShortner.exception.ConflictException;
import com.urlShortner.exception.GoneException;
import com.urlShortner.exception.ResourceNotFoundException;
import com.urlShortner.repository.UrlRepository;
import com.urlShortner.repository.UserRepository;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlShortenerService {

	private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final int GENERATED_CODE_LENGTH = 8;

	private final UrlRepository urlRepository;
	private final UserRepository userRepository;
	private final String publicBaseUrl;

	public UrlShortenerService(
			UrlRepository urlRepository,
			UserRepository userRepository,
			@Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
		this.urlRepository = urlRepository;
		this.userRepository = userRepository;
		this.publicBaseUrl = normalizeBaseUrl(publicBaseUrl);
	}

	@Transactional
	public UrlResponse create(CreateUrlRequest request, String ownerEmail) {
		User user = findUser(ownerEmail);
		String originalUrl = normalizeAndValidateUrl(request.originalUrl());
		String shortCode = resolveAvailableShortCode(request.customShortCode());
		Instant now = Instant.now();
		Instant expiresAt = request.expiresAt();
		if (expiresAt != null && expiresAt.isBefore(now)) {
			throw new BadRequestException("Expiration time must be in the future");
		}

		Url url = new Url();
		url.setUser(user);
		url.setOriginalUrl(originalUrl);
		url.setShortCode(shortCode);
		url.setCreatedAt(now);
		url.setExpiresAt(expiresAt);
		url.setClicks(0L);
		url.setLastAccessed(null);
		url = urlRepository.save(url);

		return toResponse(url);
	}

	@Transactional(readOnly = true)
	public List<UrlResponse> listAll(String ownerEmail, boolean admin) {
		List<Url> urls = admin
				? urlRepository.findAllByOrderByCreatedAtDesc()
				: urlRepository.findAllByUser_EmailOrderByCreatedAtDesc(normalizeOwnerEmail(ownerEmail));
		return urls.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<UrlResponse> listExpired(String ownerEmail, boolean admin) {
		Instant now = Instant.now();
		List<Url> urls = admin
				? urlRepository.findByExpiresAtLessThanEqual(now)
				: urlRepository.findByUser_EmailAndExpiresAtLessThanEqual(normalizeOwnerEmail(ownerEmail), now);
		return urls.stream().map(this::toResponse).toList();
	}

	@Transactional
	public long purgeExpiredUrls() {
		Instant now = Instant.now();
		List<Url> expired = urlRepository.findByExpiresAtLessThanEqual(now);
		if (expired.isEmpty()) {
			return 0L;
		}

		urlRepository.deleteAll(expired);
		return expired.size();
	}

	@Transactional
	public void deleteById(UUID id, String ownerEmail, boolean admin) {
		Url url = admin
				? urlRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("URL not found: " + id))
				: urlRepository.findByIdAndUser_Email(id, normalizeOwnerEmail(ownerEmail))
						.orElseThrow(() -> new ResourceNotFoundException("URL not found: " + id));
		urlRepository.delete(url);
	}

	@Transactional
	public UrlResponse updateUrl(UUID id, String ownerEmail, boolean admin, UpdateUrlRequest request) {
		Url url = admin
				? urlRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("URL not found: " + id))
				: urlRepository.findByIdAndUser_Email(id, normalizeOwnerEmail(ownerEmail))
					.orElseThrow(() -> new ResourceNotFoundException("URL not found: " + id));

		String originalUrl = normalizeAndValidateUrl(request.originalUrl());
		url.setOriginalUrl(originalUrl);
		return toResponse(urlRepository.save(url));
	}

	@Transactional(readOnly = true)
	public AnalyticsResponse getAnalytics(String shortCode, String ownerEmail, boolean admin) {
		Url url = admin ? findUrl(shortCode) : findOwnedUrl(shortCode, ownerEmail);
		return new AnalyticsResponse(
				url.getShortCode(),
				url.getClicks(),
				url.getCreatedAt(),
				url.getLastAccessed(),
				url.getExpiresAt());
	}

	@Transactional
	public Url resolveUrlForRedirect(String shortCode) {
		Url url = findUrl(shortCode);
		if (url.isExpired(Instant.now())) {
			throw new GoneException("This short URL has expired");
		}

		url.setClicks(url.getClicks() + 1);
		url.setLastAccessed(Instant.now());
		return urlRepository.save(url);
	}

	private Url findUrl(String shortCode) {
		String normalizedShortCode = normalizeShortCode(shortCode);
		return urlRepository.findByShortCode(normalizedShortCode)
				.orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode));
	}

	private Url findOwnedUrl(String shortCode, String ownerEmail) {
		String normalizedShortCode = normalizeShortCode(shortCode);
		return urlRepository.findByShortCodeAndUser_Email(normalizedShortCode, normalizeOwnerEmail(ownerEmail))
				.orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode));
	}

	private UrlResponse toResponse(Url url) {
		return new UrlResponse(
				url.getId(),
				url.getOriginalUrl(),
				url.getShortCode(),
				buildShortUrl(url.getShortCode()),
				url.getCreatedAt(),
				url.getExpiresAt(),
				url.getClicks(),
				url.getLastAccessed());
	}

	private User findUser(String ownerEmail) {
		return userRepository.findByEmail(normalizeOwnerEmail(ownerEmail))
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
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
		String value = Optional.ofNullable(shortCode)
				.orElseThrow(() -> new BadRequestException("Short code is required"))
				.trim();
		if (!value.matches("^[A-Za-z0-9_-]{4,32}$")) {
			throw new BadRequestException("Short code must be 4 to 32 characters and use letters, numbers, hyphen or underscore");
		}
		return value;
	}

	private String normalizeAndValidateUrl(String originalUrl) {
		String value = Optional.ofNullable(originalUrl)
				.orElseThrow(() -> new BadRequestException("Original URL is required"))
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
