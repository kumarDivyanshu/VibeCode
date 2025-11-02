package com.vibecode.aihelper.services;

import com.vibecode.aihelper.converters.AiHelperConverter;
import com.vibecode.aihelper.dtos.AiChatDtos;
import com.vibecode.aihelper.exceptions.ResourceNotFoundException;
import com.vibecode.aihelper.exceptions.ServiceLogicException;
import com.vibecode.aihelper.modals.AiChatHistory;
import com.vibecode.aihelper.modals.AiChatSession;
import com.vibecode.aihelper.modals.ERole;
import com.vibecode.aihelper.modals.Question;
import com.vibecode.aihelper.repositories.AiChatHistoryRepository;
import com.vibecode.aihelper.repositories.AiChatSessionRepository;
import com.vibecode.aihelper.repositories.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// Added imports for robust JSON extraction
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class AiHelperService {
    
    @Autowired
    private AiChatSessionRepository aiChatSessionRepository;

    @Autowired
    private AiChatHistoryRepository aiChatHistoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private ChatClient chatClient;

    private final SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("""
            You are a friendly, patient, and expert coding tutor for the Placify education platform. 
            Your job is to help learners solve coding problems by teaching and guiding — not by handing them the full solution up-front.
            
            Core rules:
            - You will be given: the problem statement (question details), learner's current code and the past chat history. Answer using that context; do not repeat information already available unless it is needed for clarity.
            - Never give the final, complete solution (full program) as the first response. Instead, help the learner arrive at the solution with step-by-step guidance and Socratic questioning.
            - Always teach best practices when relevant: readability, naming, algorithmic complexity, edge cases, tests, and maintainability.
            - Adapt your language and depth to the learner’s skill level (use the chat history to infer beginner / intermediate / advanced).
            - Be polite, encouraging, and concise. Avoid condescension.
            
            Progressive hinting policy (how to guide):
            - Big-picture hint: Give a short, conceptual outline (1–3 sentences) describing an approach or data structure to consider.
            - Strategy + decomposition: Break the approach into small, numbered sub-tasks. For each subtask, give a short hint or question that nudges the learner to try it.
            - Pseudocode / skeleton: Provide pseudocode or a small code skeleton for one subtask (not the whole solution). Explain the reasoning and complexity of the skeleton.
            - Focused snippet / debugging tips: If the learner is still stuck, reveal a very small concrete code snippet (4–10 lines maximum) that solves a single subtask or demonstrates a key idea. Explain what it does and why.
            - Reveal full solution only if explicitly requested (the user must ask: “Show full solution” or similar). If they request full solution, ask what language and whether they want tests / explanation, then provide an explained, well-documented solution.

            Tone & behaviour:
            - Encourage experimentation and short iterations.
            - Praise effort before critique. Be direct about errors but constructive.
            - Keep responses compact — learners should get one clear next action per message.

            Output Rules:
            - The "answer" field must be a single JSON string.
            - Escape all double quotes inside the answer using backslash (\").
            - Replace any line breaks with \n.
            - Do NOT include extra text outside the JSON object.
            - Do not use single quotes for JSON keys.
            - Keep the answer concise and formatted for JSON parsing.

            Question Details:
            
                Title: {title},
                Description: {description},
                InputFormat: {inputFormat},
                OutputFormat: {outputFormat},
                Constraints: {constraints},
                SampleInput: {sampleInput},
                SampleOutput: {sampleOutput},
                TimeLimit: {timeLimit},
                MemoryLimit: {memoryLimit},
            
            Past chat History:
                {chatHistoryText}
             """
    );

    public AiHelperService(AiChatSessionRepository aiChatSessionRepository, AiChatHistoryRepository aiChatHistoryRepository, ChatClient.Builder chatClientBuilder) {
        this.aiChatSessionRepository = aiChatSessionRepository;
        this.aiChatHistoryRepository = aiChatHistoryRepository;
        this.chatClient = chatClientBuilder.build();
    }

    public ResponseEntity<AiChatDtos.ChatQueryResponse> processQuery(String userId, AiChatDtos.Request aiChatRequest) throws AccessDeniedException {

        try {
            AiChatSession session = aiChatSessionRepository
                    .findById(aiChatRequest.sessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Session Not found: " + aiChatRequest.sessionId()));

            if (!Objects.equals(userId, session.getUserId())) {
                throw new AccessDeniedException("The user" + userId + " has not permission to access to particular resource: " + aiChatRequest.sessionId() );
            }

            Question question = questionRepository
                    .findById(session.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question Not found: " + session.getQuestionId()));
        
        // Save user query to database
            AiChatHistory queryChat = AiChatHistory.builder()
                    .sessionId(session.getSessionId())
                    .role(ERole.ROLE_USER)
                    .text(aiChatRequest.query())
                    .datetime(LocalDateTime.now())
                    .build();
            aiChatHistoryRepository.save(queryChat);

        // Get last 20 chathistory
            List<AiChatHistory> chatHistoryList = aiChatHistoryRepository.findTop20BySessionIdOrderByDatetimeDesc(session.getSessionId());
            Collections.reverse(chatHistoryList);
            String chatHistoryText = chatHistoryList.stream()
                .map(chat -> {
                        String role = chat.getRole().toString();
                        String roleName = role.contains("_") ? role.substring(role.indexOf('_') + 1) : role;
                        return "%s: %s\n".formatted(roleName, chat.getText());
                })
                .collect(Collectors.joining());

        // Contruct prompt
            Message systemMessage = systemPromptTemplate
                    .createMessage(
                            Map.of(
                                    "title", question.getTitle(),
                                    "description", question.getDescription(),
                                    "inputFormat", question.getInputFormat(),
                                    "outputFormat", question.getOutputFormat(),
                                    "constraints", question.getConstraints(),
                                    "sampleInput", question.getSampleTestInput(),
                                    "sampleOutput", question.getSampleTestOutput(),
                                    "timeLimit", question.getTimeLimit(),
                                    "memoryLimit", question.getMemoryLimit(),
                                    "chatHistoryText", chatHistoryText
                            )
                    );
            Message userMessage = new UserMessage(
                    """
                    My code:
                    %s
                    
                    My question:
                    %s
                    """.formatted(aiChatRequest.code(), aiChatRequest.query())
            );

            // Build the prompt without model-specific JSON response options; rely on prompt rules and safe parsing
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // Send request to LLM and handle possibly unstructured content safely
            String rawContent = this.chatClient
                    .prompt(prompt)
                    .call()
                    .content();

            String answer = extractAnswerSafely(rawContent);

            AiChatDtos.ChatQueryResponse chatQueryResponse = new AiChatDtos.ChatQueryResponse(answer);

        // Store LLM response to database
            AiChatHistory responseChat = AiChatHistory.builder()
                    .sessionId(session.getSessionId())
                    .text(chatQueryResponse.answer())
                    .role(ERole.ROLE_ASSISTANT)
                    .datetime(LocalDateTime.now())
                    .build();
            aiChatHistoryRepository.save(responseChat);

            return ResponseEntity.ok(chatQueryResponse);
        } catch (AccessDeniedException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceLogicException(e.getMessage());
        }

    }

    public ResponseEntity<AiChatDtos.ChatHistoryResponse> getHistory(String userId, @RequestParam("questionId") String questionId) {
        try {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Question Not found: " + questionId));

            AiChatSession session = aiChatSessionRepository
                    .findByUserIdAndQuestionId(userId, questionId)
                    .orElseGet(() ->
                                aiChatSessionRepository.save(
                                AiChatSession.builder()
                                        .questionId(questionId)
                                        .userId(userId)
                                        .build()
                                )
                    );

            List<AiChatDtos.ChatItem> chatItems = aiChatHistoryRepository
                    .findBySessionId(session.getSessionId())
                    .stream()
                    .map(AiHelperConverter::aiChatHistoryToAiChatItem)
                    .toList();

            return ResponseEntity.ok(new AiChatDtos.ChatHistoryResponse(chatItems, session.getSessionId()));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceLogicException(e.getMessage());
        }
    }

    // --- Helpers ---
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Tries to extract the "answer" field from a JSON object if present; otherwise returns the raw text.
     * Also strips common code fences like ```json ... ```.
     */
    private static String extractAnswerSafely(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String cleaned = stripCodeFences(raw).trim();
        // Try to locate a JSON object within the content
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            String json = cleaned.substring(start, end + 1);
            try {
                JsonNode node = MAPPER.readTree(json);
                JsonNode ans = node.get("answer");
                if (ans != null && !ans.isNull()) {
                    return ans.asText();
                }
            } catch (Exception ignore) {
                // fall through to return cleaned text
            }
        }
        return cleaned;
    }

    private static String stripCodeFences(String s) {
        // Remove common markdown code fences
        String withoutFences = s.replaceAll("(?s)```+\\w*\\s*", "");
        withoutFences = withoutFences.replace("```", "");
        return withoutFences;
    }
}
