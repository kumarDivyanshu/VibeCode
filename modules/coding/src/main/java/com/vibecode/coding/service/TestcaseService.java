package com.vibecode.coding.service;

import com.vibecode.coding.entity.Question;
import com.vibecode.coding.entity.Testcase;
import com.vibecode.coding.exceptions.ResourceNotFoundException;
import com.vibecode.coding.repository.QuestionRepository;
import com.vibecode.coding.repository.TestcaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestcaseService {
    private final TestcaseRepository testcaseRepository;
    private final QuestionRepository questionRepository;

    public List<Testcase> list(String questionId, Boolean includeHidden) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        if (includeHidden == null) includeHidden = false;
        if (includeHidden) return testcaseRepository.findByQuestion(question);
        return testcaseRepository.findByQuestionAndHidden(question, false);
    }

    public Testcase get(String id) {
        return testcaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Testcase not found"));
    }

    @Transactional
    public Testcase create(String questionId, Testcase t) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        if (t.getId() == null || t.getId().isBlank()) t.setId(UUID.randomUUID().toString());
        if (t.getTimeLimit() == null) t.setTimeLimit(new BigDecimal("2.00"));
        if (t.getMemoryLimit() == null) t.setMemoryLimit(new BigDecimal("256.00"));
        t.setQuestion(question);
        return testcaseRepository.save(t);
    }

    @Transactional
    public Testcase update(String id, Testcase updates) {
        Testcase existing = get(id);
        existing.setInput(updates.getInput());
        existing.setExpectedOutput(updates.getExpectedOutput());
        if (updates.getHidden() != null) existing.setHidden(updates.getHidden());
        if (updates.getTimeLimit() != null) existing.setTimeLimit(updates.getTimeLimit());
        if (updates.getMemoryLimit() != null) existing.setMemoryLimit(updates.getMemoryLimit());
        return testcaseRepository.save(existing);
    }

    @Transactional
    public void delete(String id) {
        Testcase existing = get(id);
        testcaseRepository.delete(existing);
    }
}

