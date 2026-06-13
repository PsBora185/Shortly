package com.urlShortner.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.urlShortner.entity.UrlMappingEntity;

public interface UrlRepository extends JpaRepository<UrlMappingEntity, UUID> {

	Optional<UrlMappingEntity> findByShortCode(String shortCode);

	Optional<UrlMappingEntity> findByShortCodeAndOwnerEmail(String shortCode, String ownerEmail);

	Optional<UrlMappingEntity> findByIdAndOwnerEmail(UUID id, String ownerEmail);

	List<UrlMappingEntity> findAllByOrderByCreatedAtDesc();

	List<UrlMappingEntity> findAllByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);

	List<UrlMappingEntity> findByExpiresAtLessThanEqual(Instant instant);

	List<UrlMappingEntity> findByOwnerEmailAndExpiresAtLessThanEqual(String ownerEmail, Instant instant);
}
