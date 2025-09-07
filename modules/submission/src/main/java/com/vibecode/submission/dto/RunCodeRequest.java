package com.vibecode.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RunCodeRequest {
    @NotNull
    private Integer languageId;
    @NotBlank
    private String sourceCode;
    private String stdin; // optional input
}

