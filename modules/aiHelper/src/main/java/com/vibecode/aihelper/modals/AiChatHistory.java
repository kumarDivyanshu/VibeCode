package com.vibecode.aihelper.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiChatHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    private Long sessionId;

    private String text;

    @Convert(converter = com.vibecode.aihelper.modals.converters.RoleConverter.class)
    @Column(name = "role", nullable = false, columnDefinition = "ENUM('user','assistant')")
    private ERole role;
    
    private LocalDateTime datetime;
}
