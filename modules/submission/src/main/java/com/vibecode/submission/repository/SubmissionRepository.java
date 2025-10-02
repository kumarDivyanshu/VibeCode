package com.vibecode.submission.repository;

import com.vibecode.submission.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, String> {
    List<Submission> findByUserIdOrderBySubmissionTimeDesc(String userId);
}

