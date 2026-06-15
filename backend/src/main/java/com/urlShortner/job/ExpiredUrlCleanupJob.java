package com.urlShortner.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.urlShortner.repository.OtpSessionRepository;
import com.urlShortner.service.UrlShortenerService;

@Component
public class ExpiredUrlCleanupJob {

	private static final Logger logger = LoggerFactory.getLogger(ExpiredUrlCleanupJob.class);

	private final UrlShortenerService urlShortenerService;
	private final OtpSessionRepository otpSessionRepository;

	public ExpiredUrlCleanupJob(UrlShortenerService urlShortenerService, OtpSessionRepository otpSessionRepository) {
		this.urlShortenerService = urlShortenerService;
		this.otpSessionRepository = otpSessionRepository;
	}

	@Scheduled(fixedDelayString = "${app.cleanup.fixed-delay-ms:21600000}")
	public void purgeExpiredUrls() {
		long deletedCount = urlShortenerService.purgeExpiredUrls();
		if (deletedCount > 0) {
			logger.info("Purged {} expired short URL(s)", deletedCount);
		}
	}

	@Scheduled(fixedDelay = 300000) // every 5 minutes
	@org.springframework.transaction.annotation.Transactional
	public void purgeExpiredOtpSessions() {
		int deleted = otpSessionRepository.deleteExpiredSessions(java.time.Instant.now());
		if (deleted > 0) {
			logger.info("Purged {} expired OTP session(s)", deleted);
		}
	}
}
