package com.vibecode.coding.controllers;

import com.vibecode.coding.entity.Question;
import com.vibecode.coding.entity.Question.Difficulty;
import com.vibecode.coding.service.QuestionService;
import com.vibecode.coding.dto.QuestionDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping
    public Page<QuestionDtos.Response> list(@RequestParam(required = false) Difficulty difficulty,
                                            @RequestParam(required = false) String title,
                                            Pageable pageable) {
        return questionService.list(java.util.Optional.ofNullable(difficulty), java.util.Optional.ofNullable(title), pageable)
                .map(q -> new QuestionDtos.Response(
                        q.getId(), q.getTitle(), q.getDescription(), q.getInputFormat(), q.getOutputFormat(),
                        q.getConstraints(), q.getSampleTestInput(), q.getSampleTestOutput(), q.getDifficulty(), q.getTimeLimit(), q.getMemoryLimit()
                ));
    }

    @GetMapping("/{id}")
    public QuestionDtos.Response get(@PathVariable String id) {
        Question q = questionService.get(id);
        return new QuestionDtos.Response(
                q.getId(), q.getTitle(), q.getDescription(), q.getInputFormat(), q.getOutputFormat(),
                q.getConstraints(), q.getSampleTestInput(), q.getSampleTestOutput(), q.getDifficulty(), q.getTimeLimit(), q.getMemoryLimit()
        );
    }

//    @PreAuthorize("hasAnyAuthority('ADMIN','ROLE_ADMIN','admin')")
    @PostMapping("/add")
    public ResponseEntity<QuestionDtos.Response> create(@Valid @RequestBody QuestionDtos.Create req) {
        Question created = questionService.create(Question.builder()
                .title(req.title())
                .description(req.description())
                .inputFormat(req.inputFormat())
                .outputFormat(req.outputFormat())
                .constraints(req.constraints())
                .sampleTestInput(req.sampleTestInput())
                .sampleTestOutput(req.sampleTestOutput())
                .difficulty(req.difficulty())
                .timeLimit(req.timeLimit())
                .memoryLimit(req.memoryLimit())
                .build());
        return ResponseEntity.ok(new QuestionDtos.Response(
                created.getId(), created.getTitle(), created.getDescription(), created.getInputFormat(), created.getOutputFormat(),
                created.getConstraints(), created.getSampleTestInput(), created.getSampleTestOutput(), created.getDifficulty(), created.getTimeLimit(), created.getMemoryLimit()
        ));
    }

//    @PreAuthorize("hasAnyAuthority('ADMIN','ROLE_ADMIN','admin')")
    @PutMapping("/{id}")
    public QuestionDtos.Response update(@PathVariable String id, @Valid @RequestBody QuestionDtos.Update req) {
        Question updated = questionService.update(id, Question.builder()
                .title(req.title())
                .description(req.description())
                .inputFormat(req.inputFormat())
                .outputFormat(req.outputFormat())
                .constraints(req.constraints())
                .sampleTestInput(req.sampleTestInput())
                .sampleTestOutput(req.sampleTestOutput())
                .difficulty(req.difficulty())
                .timeLimit(req.timeLimit())
                .memoryLimit(req.memoryLimit())
                .build());
        return new QuestionDtos.Response(
                updated.getId(), updated.getTitle(), updated.getDescription(), updated.getInputFormat(), updated.getOutputFormat(),
                updated.getConstraints(), updated.getSampleTestInput(), updated.getSampleTestOutput(), updated.getDifficulty(), updated.getTimeLimit(), updated.getMemoryLimit()
        );
    }

//    @PreAuthorize("hasAnyAuthority('ADMIN','ROLE_ADMIN','admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
