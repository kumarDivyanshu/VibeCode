package com.vibecode.submission.dto;

import com.vibecode.submission.entity.SubmissionResultStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class SubmissionResultResponse {
    private String id;
    private String submissionId;
    private String testcaseId;
    private SubmissionResultStatus status;
    private BigDecimal executionTime;
    private BigDecimal memoryUsed;
    private String output;
    private String errorMessage;
    private Instant createdAt;
}

