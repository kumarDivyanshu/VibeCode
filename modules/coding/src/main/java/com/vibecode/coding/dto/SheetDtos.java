package com.vibecode.coding.dto;

import java.util.List;

public class SheetDtos {

    public record Create(
            String sheetName,
            String description
    ) {}

    public record Response(
            Integer id,
            String sheetName,
            String description
    ) {}

    public record AddQuestions(
            List<String> questionIds
    ) {}

    public record AddQuestionsResponse(
            Integer sheetId,
            int added,
            int skipped,
            List<String> addedQuestionIds,
            List<String> skippedQuestionIds
    ) {}
}

