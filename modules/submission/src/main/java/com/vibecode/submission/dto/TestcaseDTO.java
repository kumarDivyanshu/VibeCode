package com.vibecode.submission.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestcaseDTO {
    private String id;
    private String input;
    private String expectedOutput;
    private boolean hidden;
}
