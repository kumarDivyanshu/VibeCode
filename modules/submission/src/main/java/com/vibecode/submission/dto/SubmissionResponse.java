package com.vibecode.submission.dto;

import com.vibecode.submission.entity.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class SubmissionResponse {
    private String id;
    private String questionId;
    private String userId;
    private Integer languageId;
    private SubmissionStatus status;
    private int testcasesPassed;
    private int testcasesFailed;
    private int totalTestcases;
    private BigDecimal executionTime;
    private BigDecimal memoryUsed;
    private int score;
    private Instant submissionTime;
}

