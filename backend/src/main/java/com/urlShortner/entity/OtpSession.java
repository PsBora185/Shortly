package com.urlShortner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "otp_sessions")
public class OtpSession {

	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
	private UUID id;

	@Column(name = "email", nullable = false, length = 320)
	private String email;

	@Column(name = "otp_code", nullable = false, length = 6)
	private String otpCode;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "verified", nullable = false)
	private boolean verified = false;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
}
