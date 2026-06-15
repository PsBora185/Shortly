package com.urlShortner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
	private UUID id;

	@Column(name = "full_name", nullable = false, length = 120)
	private String fullName;

	@Column(name = "email", nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "password_hash", length = 120)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, length = 20)
	private AuthProvider provider;

	@Column(name = "provider_id", length = 120)
	private String providerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private UserRole role;

	@Column(name = "enabled", nullable = false)
	private boolean enabled = true;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "last_login_at")
	private Instant lastLoginAt;

	@JsonIgnore
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<Url> urls = new ArrayList<>();
}
