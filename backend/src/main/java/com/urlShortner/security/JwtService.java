package com.urlShortner.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlShortner.entity.AppUser;
import com.urlShortner.entity.UserRole;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

	private final ObjectMapper objectMapper;
	private final byte[] secret;
	private final long expirationMillis;

	public JwtService(
			ObjectMapper objectMapper,
			@Value("${app.jwt.secret}") String jwtSecret,
			@Value("${app.jwt.expiration-ms:259200000}") long expirationMillis) {
		this.objectMapper = objectMapper;
		this.secret = jwtSecret.getBytes(StandardCharsets.UTF_8);
		this.expirationMillis = expirationMillis;
	}

	public AuthToken issueToken(AppUser user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusMillis(expirationMillis);

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", user.getEmail());
		payload.put("name", user.getFullName());
		payload.put("role", user.getRole().name());
		payload.put("provider", user.getProvider().name());
		payload.put("iat", now.getEpochSecond());
		payload.put("exp", expiresAt.getEpochSecond());

		String token = sign(payload);
		return new AuthToken(token, expiresAt, expirationMillis / 1000);
	}

	public JwtPrincipal parse(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new IllegalArgumentException("Invalid JWT format");
		}

		String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		String unsignedToken = parts[0] + "." + parts[1];
		if (!constantTimeEquals(parts[2], signPart(unsignedToken))) {
			throw new IllegalArgumentException("Invalid JWT signature");
		}

		Map<String, Object> payload = readPayload(payloadJson);
		long exp = Long.parseLong(String.valueOf(payload.get("exp")));
		if (Instant.now().getEpochSecond() >= exp) {
			throw new IllegalArgumentException("JWT expired");
		}

		String email = String.valueOf(payload.get("sub"));
		String fullName = String.valueOf(payload.getOrDefault("name", email));
		String role = String.valueOf(payload.getOrDefault("role", UserRole.USER.name()));
		return new JwtPrincipal(email, fullName, role, List.of("ROLE_" + role));
	}

	private String sign(Map<String, Object> payload) {
		try {
			String header = base64Url(JWT_HEADER.getBytes(StandardCharsets.UTF_8));
			String payloadJson = objectMapper.writeValueAsString(payload);
			String payloadEncoded = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
			String unsignedToken = header + "." + payloadEncoded;
			return unsignedToken + "." + signPart(unsignedToken);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Unable to create JWT", exception);
		}
	}

	private String signPart(String unsignedToken) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
			byte[] signature = mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8));
			return base64Url(signature);
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to sign JWT", exception);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> readPayload(String json) {
		try {
			return objectMapper.readValue(json, LinkedHashMap.class);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("Invalid JWT payload", exception);
		}
	}

	private String base64Url(byte[] data) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
	}

	private boolean constantTimeEquals(String expected, String actual) {
		return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
	}

	public record AuthToken(String token, Instant expiresAt, long expiresInSeconds) {
	}
}
