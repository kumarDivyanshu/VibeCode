package com.vibecode.submission.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RunCodeResponse {
    private String token; // Judge0 token for the run
    private String stdout;
    private String stderr;
    private String compileOutput;
    private String message;
    private String status; // textual description
    private Integer statusId; // judge0 status id
    private String time; // as string from judge0
    private String memory; // as string from judge0
}

