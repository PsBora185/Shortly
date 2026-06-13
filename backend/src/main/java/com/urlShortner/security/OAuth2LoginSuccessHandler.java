package com.urlShortner.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlShortner.dto.AuthResponse;
import com.urlShortner.entity.AppUser;
import com.urlShortner.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final AuthService authService;
	private final ObjectMapper objectMapper;

	public OAuth2LoginSuccessHandler(AuthService authService, ObjectMapper objectMapper) {
		this.authService = authService;
		this.objectMapper = objectMapper;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		OAuth2User principal = (OAuth2User) authentication.getPrincipal();
		String email = String.valueOf(principal.getAttributes().get("email"));
		if (email == null || email.isBlank() || "null".equalsIgnoreCase(email)) {
			throw new IllegalArgumentException("Google account did not provide an email address");
		}
		String fullName = String.valueOf(principal.getAttributes().getOrDefault("name", email));
		String providerId = String.valueOf(principal.getAttributes().getOrDefault("sub", email));

		AppUser user = authService.upsertGoogleUser(email, fullName, providerId);
		AuthResponse authResponse = authService.issueToken(user);

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		objectMapper.writeValue(response.getWriter(), authResponse);
	}
}
