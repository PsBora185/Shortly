package com.urlShortner.config;

import com.urlShortner.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

	private final AuthService authService;
	private final String adminEmail;
	private final String adminPassword;
	private final String adminName;

	public DataInitializer(
			AuthService authService,
			@Value("${app.admin.email:}") String adminEmail,
			@Value("${app.admin.password:}") String adminPassword,
			@Value("${app.admin.full-name:Administrator}") String adminName) {
		this.authService = authService;
		this.adminEmail = adminEmail;
		this.adminPassword = adminPassword;
		this.adminName = adminName;
	}

	@Override
	public void run(String... args) {
		if (adminEmail != null && !adminEmail.isBlank() && adminPassword != null && !adminPassword.isBlank()) {
			authService.seedAdmin(adminEmail, adminPassword, adminName);
		}
	}
}
