package com.urlShortner.security;

import java.util.List;

public record JwtPrincipal(
		String email,
		String fullName,
		String role,
		List<String> authorities) {
}
