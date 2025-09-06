package com.vibecode.coding.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class TestcaseDtos {
    public record Create(
            @NotBlank String input,
            @NotBlank String expectedOutput,
            Boolean hidden,
            BigDecimal timeLimit,
            BigDecimal memoryLimit
    ) {}

    public record Update(
            @NotBlank String input,
            @NotBlank String expectedOutput,
            Boolean hidden,
            BigDecimal timeLimit,
            BigDecimal memoryLimit
    ) {}

    public record Response(
            String id,
            String questionId,
            String input,
            String expectedOutput,
            boolean hidden,
            BigDecimal timeLimit,
            BigDecimal memoryLimit
    ) {}
}

