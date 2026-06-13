package com.urlShortner.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.urlShortner.service.UrlShortenerService;

@Component
public class ExpiredUrlCleanupJob {

	private static final Logger logger = LoggerFactory.getLogger(ExpiredUrlCleanupJob.class);

	private final UrlShortenerService urlShortenerService;

	public ExpiredUrlCleanupJob(UrlShortenerService urlShortenerService) {
		this.urlShortenerService = urlShortenerService;
	}

	@Scheduled(fixedDelayString = "${app.cleanup.fixed-delay-ms:21600000}")
	public void purgeExpiredUrls() {
		long deletedCount = urlShortenerService.purgeExpiredUrls();
		if (deletedCount > 0) {
			logger.info("Purged {} expired short URL(s)", deletedCount);
		}
	}
}
