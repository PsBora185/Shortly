package com.urlShortner.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "urls")
public class Url {

	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
	private UUID id;

	@Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
	private String originalUrl;

	@Column(name = "short_code", nullable = false, unique = true, length = 32)
	private String shortCode;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Column(name = "clicks", nullable = false)
	private long clicks;

	@Column(name = "last_accessed")
	private Instant lastAccessed;

	public boolean isExpired(Instant now) {
		return expiresAt != null && expiresAt.isBefore(now);
	}
}
