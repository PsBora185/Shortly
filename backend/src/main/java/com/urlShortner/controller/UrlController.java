package com.urlShortner.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urlShortner.dto.AnalyticsResponse;
import com.urlShortner.dto.CreateUrlRequest;
import com.urlShortner.dto.PurgeExpiredResponse;
import com.urlShortner.dto.UpdateUrlRequest;
import com.urlShortner.dto.UrlResponse;
import com.urlShortner.service.UrlShortenerService;

@RestController
@Validated
@RequestMapping
public class UrlController {

	private final UrlShortenerService urlShortenerService;

	public UrlController(UrlShortenerService urlShortenerService) {
		this.urlShortenerService = urlShortenerService;
	}

	@PostMapping("/api/urls")
	public ResponseEntity<UrlResponse> create(Authentication authentication, @Valid @RequestBody CreateUrlRequest request) {
		UrlResponse response = urlShortenerService.create(request, authentication.getName());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/api/urls")
	public ResponseEntity<List<UrlResponse>> listAll(Authentication authentication) {
		return ResponseEntity.ok(urlShortenerService.listAll(authentication.getName(), isAdmin(authentication)));
	}

	@DeleteMapping("/api/urls/{id}")
	public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
		urlShortenerService.deleteById(id, authentication.getName(), isAdmin(authentication));
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/api/urls/{id}")
	public ResponseEntity<UrlResponse> update(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody UpdateUrlRequest request) {
		UrlResponse response = urlShortenerService.updateUrl(id, authentication.getName(), isAdmin(authentication), request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/analytics/{shortCode}")
	public ResponseEntity<AnalyticsResponse> analytics(Authentication authentication, @PathVariable String shortCode) {
		return ResponseEntity.ok(urlShortenerService.getAnalytics(shortCode, authentication.getName(), isAdmin(authentication)));
	}

	@GetMapping("/api/admin/urls/expired")
	public ResponseEntity<List<UrlResponse>> expired(Authentication authentication) {
		return ResponseEntity.ok(urlShortenerService.listExpired(authentication.getName(), isAdmin(authentication)));
	}

	@DeleteMapping("/api/admin/urls/expired")
	public ResponseEntity<PurgeExpiredResponse> purgeExpired(Authentication authentication) {
		return ResponseEntity.ok(new PurgeExpiredResponse(urlShortenerService.purgeExpiredUrls()));
	}

	@GetMapping("/{shortCode:[A-Za-z0-9_-]{4,32}}")
	public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
		String destination = urlShortenerService.resolveUrlForRedirect(shortCode).getOriginalUrl();
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(destination)).build();
	}

	private boolean isAdmin(Authentication authentication) {
		return authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
	}
}
