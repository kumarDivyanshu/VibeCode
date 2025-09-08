package com.vibecode.submission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submission_results")
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionResult {
    @Id
    private String id;

    @Column(name = "submission_id", nullable = false)
    private String submissionId;

    @Column(name = "testcase_id", nullable = false)
    private String testcaseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubmissionResultStatus status;

    @Column(name = "execution_time", precision = 8, scale = 2)
    private BigDecimal executionTime;

    @Column(name = "memory_used", precision = 8, scale = 2)
    private BigDecimal memoryUsed;

    @Lob
    private String output;

    @Lob
    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist(){
        if(id == null) id = UUID.randomUUID().toString();
    }
}

