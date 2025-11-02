package com.vibecode.coding.service;

import com.vibecode.coding.dto.SheetDtos;
import com.vibecode.coding.entity.DsaSheet;
import com.vibecode.coding.entity.Question;
import com.vibecode.coding.entity.SheetQuestion;
import com.vibecode.coding.entity.SheetQuestionId;
import com.vibecode.coding.exceptions.ResourceNotFoundException;
import com.vibecode.coding.repository.SheetQuestionRepository;
import com.vibecode.coding.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SheetQuestionService {

    private final SheetService sheetService;
    private final QuestionRepository questionRepository;
    private final SheetQuestionRepository sheetQuestionRepository;

    @Transactional
    public SheetDtos.AddQuestionsResponse addQuestions(Integer sheetId, List<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            throw new IllegalArgumentException("questionIds must not be empty");
        }

        DsaSheet sheet = sheetService.getOrThrow(sheetId);

        List<Question> questions = questionRepository.findAllById(questionIds);
        Set<String> foundIds = questions.stream().map(Question::getId).collect(Collectors.toSet());

        List<String> missing = questionIds.stream()
                .filter(qid -> !foundIds.contains(qid))
                .toList();
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("Questions not found: " + missing);
        }

        List<String> added = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        List<SheetQuestion> toSave = new ArrayList<>();
        for (Question q : questions) {
            boolean exists = sheetQuestionRepository.existsBySheet_IdAndQuestion_Id(sheetId, q.getId());
            if (exists) {
                skipped.add(q.getId());
                continue;
            }
            SheetQuestion sq = SheetQuestion.builder()
                    .id(new SheetQuestionId(sheetId, q.getId()))
                    .sheet(sheet)
                    .question(q)
                    .build();
            toSave.add(sq);
            added.add(q.getId());
        }

        if (!toSave.isEmpty()) {
            sheetQuestionRepository.saveAll(toSave);
        }

        return new SheetDtos.AddQuestionsResponse(sheetId, added.size(), skipped.size(), added, skipped);
    }

    @Transactional(readOnly = true)
    public List<Question> listQuestions(Integer sheetId) {
        sheetService.getOrThrow(sheetId);
        return sheetQuestionRepository.findQuestionsForSheet(sheetId);
    }
}
