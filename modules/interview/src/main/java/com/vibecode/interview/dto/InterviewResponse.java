package com.vibecode.interview.dto;

import com.vibecode.interview.models.InterviewStatus;

import java.time.LocalDateTime;

public record InterviewResponse(
        String id,
        String userId,
        String companyId,
        String description,
        LocalDateTime scheduledDate,
        InterviewStatus status,
        Integer score,
        String feedback,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

