package com.vibecode.interview.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateInterviewRequest(
        UUID companyId,
        String description,
        LocalDateTime scheduledDate
) {}
