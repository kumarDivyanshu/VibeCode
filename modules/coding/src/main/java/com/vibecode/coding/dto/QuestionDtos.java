package com.vibecode.coding.dto;

import com.vibecode.coding.entity.Question.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class QuestionDtos {
    public record Create(
            @NotBlank @Size(max = 500) String title,
            @NotBlank String description,
            String inputFormat,
            @NotBlank String outputFormat,
            @NotBlank String constraints,
            String sampleTestInput,
            String sampleTestOutput,
            Difficulty difficulty,
            BigDecimal timeLimit,
            BigDecimal memoryLimit
    ) {}

    public record Update(
            @NotBlank @Size(max = 500) String title,
            @NotBlank String description,
            String inputFormat,
            @NotBlank String outputFormat,
            @NotBlank String constraints,
            String sampleTestInput,
            String sampleTestOutput,
            @NotNull Difficulty difficulty,
            @NotNull BigDecimal timeLimit,
            @NotNull BigDecimal memoryLimit
    ) {}

    public record Response(
            String id,
            String title,
            String description,
            String inputFormat,
            String outputFormat,
            String constraints,
            String sampleTestInput,
            String sampleTestOutput,
            Difficulty difficulty,
            BigDecimal timeLimit,
            BigDecimal memoryLimit
    ) {}
}

