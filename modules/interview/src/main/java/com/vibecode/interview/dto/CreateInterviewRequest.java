package com.vibecode.interview.dto;

import com.vibecode.interview.models.InterviewStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CreateInterviewRequest(
        @NotBlank String companyName,
        String description,
        LocalDateTime scheduledDate,
        InterviewStatus status,
        Integer score,
        String feedback
) {}
