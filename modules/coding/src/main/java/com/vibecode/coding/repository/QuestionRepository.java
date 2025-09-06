package com.vibecode.coding.repository;

import com.vibecode.coding.entity.Question;
import com.vibecode.coding.entity.Question.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, String> {
    Page<Question> findByDifficulty(Difficulty difficulty, Pageable pageable);
    Page<Question> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}

