package com.urlShortner.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.urlShortner.entity.UrlAnalyticsEntity;

public interface UrlAnalyticsRepository extends JpaRepository<UrlAnalyticsEntity, UUID> {

	Optional<UrlAnalyticsEntity> findByShortCode(String shortCode);

	List<UrlAnalyticsEntity> findAllByShortCodeIn(Collection<String> shortCodes);

	void deleteByShortCode(String shortCode);

	void deleteByShortCodeIn(Collection<String> shortCodes);
}
