package com.vibecode.submission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "submission_tokens")
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionToken {
    @Id
    private String token; // judge0 token

    @Column(name = "submission_id", nullable = false)
    private String submissionId;

    @Column(name = "testcase_id", nullable = false)
    private String testcaseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubmissionTokenStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}

