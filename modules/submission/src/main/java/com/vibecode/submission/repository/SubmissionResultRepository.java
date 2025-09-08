package com.vibecode.submission.repository;

import com.vibecode.submission.entity.SubmissionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionResultRepository extends JpaRepository<SubmissionResult, String> {
    List<SubmissionResult> findBySubmissionId(String submissionId);
    Optional<SubmissionResult> findFirstBySubmissionIdAndTestcaseId(String submissionId, String testcaseId);
}
