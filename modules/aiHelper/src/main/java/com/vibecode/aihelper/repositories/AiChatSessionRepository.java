package com.vibecode.aihelper.repositories;

import com.vibecode.aihelper.modals.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {
    Optional<AiChatSession> findByUserIdAndQuestionId(String userId, String questionId);
}
