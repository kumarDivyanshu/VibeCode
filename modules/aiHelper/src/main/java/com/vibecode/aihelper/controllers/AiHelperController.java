package com.vibecode.aihelper.controllers;

import com.vibecode.aihelper.dtos.AiChatDtos;
import com.vibecode.aihelper.services.AiHelperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class AiHelperController {

    private final AiHelperService aiHelperService;

    @PostMapping("/query")
    public ResponseEntity<AiChatDtos.ChatQueryResponse> query(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AiChatDtos.Request aiChatRequest
    ) throws AccessDeniedException {
        return aiHelperService.processQuery(userId, aiChatRequest);
    }

    @GetMapping("/history")
    public ResponseEntity<AiChatDtos.ChatHistoryResponse> history(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam String questionId
    ) {
        return aiHelperService.getHistory(userId, questionId);
    }
}
