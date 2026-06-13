package com.urlShortner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analytics")
public class UrlAnalyticsEntity {

	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
	private UUID id;

	@Column(name = "short_code", nullable = false, unique = true, length = 32)
	private String shortCode;

	@Column(name = "clicks", nullable = false)
	private long clicks;

	@Column(name = "last_accessed")
	private Instant lastAccessed;

	public UrlAnalyticsEntity() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public long getClicks() {
		return clicks;
	}

	public void setClicks(long clicks) {
		this.clicks = clicks;
	}

	public Instant getLastAccessed() {
		return lastAccessed;
	}

	public void setLastAccessed(Instant lastAccessed) {
		this.lastAccessed = lastAccessed;
	}

	public void registerClick(Instant clickedAt) {
		this.clicks++;
		this.lastAccessed = clickedAt;
	}
}
