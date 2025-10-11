package com.vibecode.aihelper.repositories;

import com.vibecode.aihelper.modals.AiChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, Long> {

    List<AiChatHistory> findBySessionId(Long sessionId);
    List<AiChatHistory> findTop20BySessionIdOrderByDatetimeDesc(Long sessionId);
}
