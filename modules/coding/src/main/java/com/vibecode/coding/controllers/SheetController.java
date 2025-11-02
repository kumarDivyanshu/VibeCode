package com.vibecode.coding.controllers;

import com.vibecode.coding.dto.SheetDtos;
import com.vibecode.coding.dto.QuestionDtos; // NEW
import com.vibecode.coding.entity.Question; // NEW
import com.vibecode.coding.service.SheetQuestionService;
import com.vibecode.coding.service.SheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; // NEW

@RestController
@RequestMapping("/sheets")
@RequiredArgsConstructor
public class SheetController {

    private final SheetService sheetService;
    private final SheetQuestionService sheetQuestionService;

    @PostMapping
    public ResponseEntity<SheetDtos.Response> createSheet(@RequestBody SheetDtos.Create req) {
        return ResponseEntity.ok(sheetService.create(req));
    }

    @PostMapping("/{sheetId}/questions")
    public ResponseEntity<SheetDtos.AddQuestionsResponse> addQuestions(
            @PathVariable Integer sheetId,
            @RequestBody SheetDtos.AddQuestions req) {
        return ResponseEntity.ok(sheetQuestionService.addQuestions(sheetId, req.questionIds()));
    }

    // NEW: list questions in a sheet
    @GetMapping("/{sheetId}/questions")
    public ResponseEntity<List<QuestionDtos.Response>> getQuestions(@PathVariable Integer sheetId) {
        List<Question> questions = sheetQuestionService.listQuestions(sheetId);
        List<QuestionDtos.Response> body = questions.stream()
                .map(q -> new QuestionDtos.Response(
                        q.getId(), q.getTitle(), q.getDescription(), q.getInputFormat(), q.getOutputFormat(),
                        q.getConstraints(), q.getSampleTestInput(), q.getSampleTestOutput(), q.getDifficulty(),
                        q.getTimeLimit(), q.getMemoryLimit()
                ))
                .toList();
        return ResponseEntity.ok(body);
    }
}

