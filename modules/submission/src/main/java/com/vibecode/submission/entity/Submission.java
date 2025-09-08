package com.vibecode.submission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Submission {
    @Id
    private String id; // UUID as string

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "language_id", nullable = false)
    private Integer languageId;

    @Lob
    @Column(name = "source_code", nullable = false)
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubmissionStatus status;

    @Column(name = "testcases_passed", nullable = false)
    private int testcasesPassed;

    @Column(name = "testcases_failed", nullable = false)
    private int testcasesFailed;

    @Column(name = "total_testcases", nullable = false)
    private int totalTestcases;

    @Column(name = "execution_time", precision = 8, scale = 2)
    private BigDecimal executionTime; // aggregated (max or sum)

    @Column(name = "memory_used", precision = 8, scale = 2)
    private BigDecimal memoryUsed; // aggregated (max)

    @Column(name = "score", nullable = false)
    private int score;

    @CreationTimestamp
    @Column(name = "submission_time", nullable = false, updatable = false)
    private Instant submissionTime;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (status == null) status = SubmissionStatus.pending;
    }
}

