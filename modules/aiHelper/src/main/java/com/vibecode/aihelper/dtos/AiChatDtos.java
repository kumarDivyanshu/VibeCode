package com.vibecode.aihelper.dtos;

import com.vibecode.aihelper.modals.ERole;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;


public class AiChatDtos {
    public record Request(
            Long sessionId,
            @NotBlank String query,
            @NotBlank String code
    ) {}

    public record ChatQueryResponse(
            String answer
    ) {}

    public record ChatItem(
            Long chatId,
            Long sessionId,
            String text,
            ERole role,
            LocalDateTime datetime
    ) {}

    public record ChatHistoryResponse(
            List<ChatItem> chats,
            Long sessionId

    ) {}
}

