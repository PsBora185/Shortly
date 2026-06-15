package com.urlShortner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.urlShortner.dto.CreateUrlRequest;
import com.urlShortner.dto.UrlResponse;
import com.urlShortner.entity.AuthProvider;
import com.urlShortner.entity.Url;
import com.urlShortner.entity.User;
import com.urlShortner.entity.UserRole;
import com.urlShortner.exception.GoneException;
import com.urlShortner.repository.UrlRepository;
import com.urlShortner.repository.UserRepository;
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
	private UserRepository userRepository;

	private UrlShortenerService urlShortenerService;

	@BeforeEach
	void setUp() {
		urlShortenerService = new UrlShortenerService(urlRepository, userRepository, "http://localhost:8080");
	}

	@Test
	void createPersistsUrlWithUserRelation() {
		User user = new User();
		user.setEmail("user@example.com");
		user.setFullName("User");
		user.setProvider(AuthProvider.LOCAL);
		user.setRole(UserRole.USER);
		when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
		when(urlRepository.findByShortCode("mycode12")).thenReturn(Optional.empty());
		when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Instant expiresAt = Instant.now().plusSeconds(3600);
		UrlResponse response = urlShortenerService.create(
				new CreateUrlRequest("https://example.com/articles/devops", "mycode12", expiresAt),
				"user@example.com");

		assertThat(response.originalUrl()).isEqualTo("https://example.com/articles/devops");
		assertThat(response.shortCode()).isEqualTo("mycode12");
		assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/mycode12");
		assertThat(response.clicks()).isZero();
		assertThat(response.expiresAt()).isEqualTo(expiresAt);

		ArgumentCaptor<Url> urlCaptor = ArgumentCaptor.forClass(Url.class);
		verify(urlRepository).save(urlCaptor.capture());
		assertThat(urlCaptor.getValue().getUser()).isSameAs(user);
		assertThat(urlCaptor.getValue().getOriginalUrl()).isEqualTo("https://example.com/articles/devops");
		assertThat(urlCaptor.getValue().getShortCode()).isEqualTo("mycode12");
	}

	@Test
	void resolveUrlForRedirectUpdatesClicksOnUrl() {
		Url url = new Url();
		url.setId(UUID.randomUUID());
		url.setOriginalUrl("https://example.com");
		url.setShortCode("devops01");
		url.setCreatedAt(Instant.parse("2026-06-13T00:00:00Z"));
		url.setClicks(3L);

		when(urlRepository.findByShortCode("devops01")).thenReturn(Optional.of(url));
		when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Url resolved = urlShortenerService.resolveUrlForRedirect("devops01");

		assertThat(resolved.getOriginalUrl()).isEqualTo("https://example.com");
		assertThat(resolved.getClicks()).isEqualTo(4L);
		assertThat(resolved.getLastAccessed()).isNotNull();
		verify(urlRepository).save(url);
	}

	@Test
	void expiredUrlsAreRejectedOnRedirect() {
		Url url = new Url();
		url.setShortCode("oldlink1");
		url.setOriginalUrl("https://example.com");
		url.setCreatedAt(Instant.parse("2026-06-13T00:00:00Z"));
		url.setExpiresAt(Instant.now().minusSeconds(60));

		when(urlRepository.findByShortCode("oldlink1")).thenReturn(Optional.of(url));

		assertThatThrownBy(() -> urlShortenerService.resolveUrlForRedirect("oldlink1"))
				.isInstanceOf(GoneException.class)
				.hasMessageContaining("expired");

		verifyNoInteractions(userRepository);
	}
}
