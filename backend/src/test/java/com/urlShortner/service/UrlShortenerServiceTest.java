package com.urlShortner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.urlShortner.dto.CreateUrlRequest;
import com.urlShortner.dto.UrlResponse;
import com.urlShortner.entity.UrlAnalyticsEntity;
import com.urlShortner.entity.UrlMappingEntity;
import com.urlShortner.exception.GoneException;
import com.urlShortner.repository.UrlAnalyticsRepository;
import com.urlShortner.repository.UrlRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

	@Mock
	private UrlRepository urlRepository;

	@Mock
	private UrlAnalyticsRepository analyticsRepository;

	private UrlShortenerService urlShortenerService;

	@BeforeEach
	void setUp() {
		urlShortenerService = new UrlShortenerService(urlRepository, analyticsRepository, "http://localhost:8080");
	}

	@Test
	void createPersistsUrlAndAnalytics() {
		when(urlRepository.findByShortCode("mycode12")).thenReturn(Optional.empty());
		when(urlRepository.save(any(UrlMappingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(analyticsRepository.save(any(UrlAnalyticsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Instant expiresAt = Instant.now().plusSeconds(3600);
		UrlResponse response = urlShortenerService.create(
				new CreateUrlRequest("https://example.com/articles/devops", "mycode12", expiresAt),
				"user@example.com");

		assertThat(response.originalUrl()).isEqualTo("https://example.com/articles/devops");
		assertThat(response.shortCode()).isEqualTo("mycode12");
		assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/mycode12");
		assertThat(response.clicks()).isZero();
		assertThat(response.expiresAt()).isEqualTo(expiresAt);

		ArgumentCaptor<UrlMappingEntity> urlCaptor = ArgumentCaptor.forClass(UrlMappingEntity.class);
		verify(urlRepository).save(urlCaptor.capture());
		assertThat(urlCaptor.getValue().getOriginalUrl()).isEqualTo("https://example.com/articles/devops");
		assertThat(urlCaptor.getValue().getShortCode()).isEqualTo("mycode12");
		verify(analyticsRepository).save(any(UrlAnalyticsEntity.class));
	}

	@Test
	void resolveUrlForRedirectRegistersClick() {
		UrlMappingEntity url = new UrlMappingEntity();
		url.setId(UUID.randomUUID());
		url.setOriginalUrl("https://example.com");
		url.setShortCode("devops01");
		url.setCreatedAt(Instant.parse("2026-06-13T00:00:00Z"));

		UrlAnalyticsEntity analytics = new UrlAnalyticsEntity();
		analytics.setShortCode("devops01");
		analytics.setClicks(3L);

		when(urlRepository.findByShortCode("devops01")).thenReturn(Optional.of(url));
		when(analyticsRepository.findByShortCode("devops01")).thenReturn(Optional.of(analytics));
		when(analyticsRepository.save(any(UrlAnalyticsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UrlMappingEntity resolved = urlShortenerService.resolveUrlForRedirect("devops01");

		assertThat(resolved.getOriginalUrl()).isEqualTo("https://example.com");
		assertThat(analytics.getClicks()).isEqualTo(4L);
		assertThat(analytics.getLastAccessed()).isNotNull();
		verify(analyticsRepository).save(analytics);
	}

	@Test
	void expiredUrlsAreRejectedOnRedirect() {
		UrlMappingEntity url = new UrlMappingEntity();
		url.setShortCode("oldlink1");
		url.setOriginalUrl("https://example.com");
		url.setCreatedAt(Instant.parse("2026-06-13T00:00:00Z"));
		url.setExpiresAt(Instant.now().minusSeconds(60));

		when(urlRepository.findByShortCode("oldlink1")).thenReturn(Optional.of(url));

		assertThatThrownBy(() -> urlShortenerService.resolveUrlForRedirect("oldlink1"))
				.isInstanceOf(GoneException.class)
				.hasMessageContaining("expired");

		verifyNoInteractions(analyticsRepository);
	}
}
