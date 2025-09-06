package com.vibecode.coding.repository;

import com.vibecode.coding.entity.Testcase;
import com.vibecode.coding.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestcaseRepository extends JpaRepository<Testcase, String> {
    List<Testcase> findByQuestion(Question question);
    List<Testcase> findByQuestionAndHidden(Question question, boolean hidden);
}

