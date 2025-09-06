package com.vibecode.coding.service;

import com.vibecode.coding.entity.Question;
import com.vibecode.coding.entity.Question.Difficulty;
import com.vibecode.coding.exceptions.ResourceNotFoundException;
import com.vibecode.coding.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    public Page<Question> list(Optional<Difficulty> difficulty, Optional<String> title, Pageable pageable) {
        if (difficulty.isPresent()) {
            return questionRepository.findByDifficulty(difficulty.get(), pageable);
        }
        if (title.isPresent() && !title.get().isBlank()) {
            return questionRepository.findByTitleContainingIgnoreCase(title.get(), pageable);
        }
        return questionRepository.findAll(pageable);
    }

    public Question get(String id) {
        return questionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Question not found"));
    }

    @Transactional
    public Question create(Question q) {
        if (q.getId() == null || q.getId().isBlank()) q.setId(UUID.randomUUID().toString());
        if (q.getTimeLimit() == null) q.setTimeLimit(new BigDecimal("2.00"));
        if (q.getMemoryLimit() == null) q.setMemoryLimit(new BigDecimal("256.00"));
        if (q.getDifficulty() == null) q.setDifficulty(Difficulty.MEDIUM);
        return questionRepository.save(q);
    }

    @Transactional
    public Question update(String id, Question updates) {
        Question existing = get(id);
        existing.setTitle(updates.getTitle());
        existing.setDescription(updates.getDescription());
        existing.setInputFormat(updates.getInputFormat());
        existing.setOutputFormat(updates.getOutputFormat());
        existing.setConstraints(updates.getConstraints());
        existing.setSampleTestInput(updates.getSampleTestInput());
        existing.setSampleTestOutput(updates.getSampleTestOutput());
        if (updates.getDifficulty() != null) existing.setDifficulty(updates.getDifficulty());
        if (updates.getTimeLimit() != null) existing.setTimeLimit(updates.getTimeLimit());
        if (updates.getMemoryLimit() != null) existing.setMemoryLimit(updates.getMemoryLimit());
        return questionRepository.save(existing);
    }

    @Transactional
    public void delete(String id) {
        Question existing = get(id);
        questionRepository.delete(existing);
    }
}

