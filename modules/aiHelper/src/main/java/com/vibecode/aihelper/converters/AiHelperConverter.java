package com.vibecode.aihelper.converters;

import com.vibecode.aihelper.dtos.AiChatDtos;
import com.vibecode.aihelper.modals.AiChatHistory;

public class AiHelperConverter {

    public static AiChatDtos.ChatItem aiChatHistoryToAiChatItem(AiChatHistory aiChatHistory) {
        return new AiChatDtos.ChatItem(
                aiChatHistory.getChatId(),
                aiChatHistory.getSessionId(),
                aiChatHistory.getText(),
                aiChatHistory.getRole(),
                aiChatHistory.getDatetime()
        );
    }
}
