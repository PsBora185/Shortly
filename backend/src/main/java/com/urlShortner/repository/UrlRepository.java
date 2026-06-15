package com.urlShortner.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.urlShortner.entity.Url;

public interface UrlRepository extends JpaRepository<Url, UUID> {

	Optional<Url> findByShortCode(String shortCode);

	Optional<Url> findByShortCodeAndUser_Email(String shortCode, String email);

	Optional<Url> findByIdAndUser_Email(UUID id, String email);

	List<Url> findAllByOrderByCreatedAtDesc();

	List<Url> findAllByUser_EmailOrderByCreatedAtDesc(String email);

	List<Url> findByExpiresAtLessThanEqual(Instant instant);

	List<Url> findByUser_EmailAndExpiresAtLessThanEqual(String email, Instant instant);
}
