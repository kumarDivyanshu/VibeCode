package com.vibecode.interview.repositories;

import com.vibecode.interview.models.Interview;
import com.vibecode.interview.models.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, String>, JpaSpecificationExecutor<Interview> {
    Page<Interview> findByUserId(String userId, Pageable pageable);
    Page<Interview> findByUserIdAndStatus(String userId, InterviewStatus status, Pageable pageable);
    Page<Interview> findByUserIdAndCompanyId(String userId, String companyId, Pageable pageable);
    Page<Interview> findByUserIdAndScheduledDateBetween(String userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Optional<Interview> findByIdAndUserId(String id, String userId);
}
