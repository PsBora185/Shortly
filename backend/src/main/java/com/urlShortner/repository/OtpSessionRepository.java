package com.urlShortner.repository;

import com.urlShortner.entity.OtpSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpSessionRepository extends JpaRepository<OtpSession, UUID> {
	Optional<OtpSession> findFirstByEmailOrderByCreatedAtDesc(String email);

	@Modifying
	@Query("DELETE FROM OtpSession o WHERE o.expiresAt < :now")
	int deleteExpiredSessions(java.time.Instant now);
}
