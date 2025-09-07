package com.vibecode.submission.dto;

import lombok.Data;

@Data
public class JudgeCallback {
    private String token; // Judge0 token
    private Integer statusId;
    private String statusDescription;
    private String stdout;
    private String stderr;
    private String compileOutput;
    private String message;
    private String time; // string as delivered
    private String memory; // string as delivered

    // Provided externally via query params or controller injection
    private String submissionId;
    private String testCaseId;
}

