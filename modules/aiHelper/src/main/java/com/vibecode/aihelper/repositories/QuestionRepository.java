package com.vibecode.aihelper.repositories;

import com.vibecode.aihelper.modals.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
}

