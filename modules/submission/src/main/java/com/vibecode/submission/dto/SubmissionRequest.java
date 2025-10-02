package com.vibecode.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionRequest {
    @NotBlank
    private String questionId;
    @NotNull
    private Integer languageId; // matches programming_languages.id
    @NotBlank
    private String sourceCode;
}

