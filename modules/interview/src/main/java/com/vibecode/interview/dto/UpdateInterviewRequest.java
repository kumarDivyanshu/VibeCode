package com.vibecode.interview.dto;

import com.vibecode.interview.models.InterviewStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateInterviewRequest(
        @Size(max = 20000) String description,
        @PastOrPresent @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime scheduledDate,
        InterviewStatus status,
        @Min(0) @Max(100) Integer score,
        @Size(max = 20000) String feedback
) {}
