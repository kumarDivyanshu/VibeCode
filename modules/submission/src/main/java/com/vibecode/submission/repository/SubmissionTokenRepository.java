package com.vibecode.submission.repository;

import com.vibecode.submission.entity.SubmissionToken;
import com.vibecode.submission.entity.SubmissionTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SubmissionTokenRepository extends JpaRepository<SubmissionToken, String> {
    List<SubmissionToken> findByStatusInAndExpiresAtAfter(List<SubmissionTokenStatus> statuses, Instant now);
    List<SubmissionToken> findBySubmissionId(String submissionId);
    Optional<SubmissionToken> findFirstBySubmissionIdAndTestcaseId(String submissionId, String testcaseId);
}
