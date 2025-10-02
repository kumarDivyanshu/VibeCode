package com.vibecode.submission.service;

import com.vibecode.submission.client.Judge0Client;
import com.vibecode.submission.client.TestcaseServiceClient;
import com.vibecode.submission.dto.*;
import com.vibecode.submission.entity.*;
import com.vibecode.submission.exception.NotFoundException;
import com.vibecode.submission.repository.SubmissionRepository;
import com.vibecode.submission.repository.SubmissionResultRepository;
import com.vibecode.submission.repository.SubmissionTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionResultRepository resultRepository;
    private final SubmissionTokenRepository tokenRepository;
    private final TestcaseServiceClient testcaseClient;
    private final Judge0Client judge0Client;

    @Value("${submission.callback-base-url:http://localhost:8092}")
    private String callbackBaseUrl;

    // Maps internal language ID to Judge0 language id. Adjust if schema differs.
    private int mapLanguageId(Integer languageId) {
        return languageId; // TODO: provide a mapping table if they diverge.
    }

    @Transactional
    public SubmissionResponse createSubmission(SubmissionRequest request, String userId) {
        System.out.println("In createSubmission, userId = " + userId);
        // Fetch testcases for the question
        List<TestcaseDTO> testcases = testcaseClient.getTestcasesForQuestion(request.getQuestionId());
        if (testcases.isEmpty()) {
            throw new NotFoundException("No testcases defined for question " + request.getQuestionId());
        }
        System.out.println("Lagusage ID = " + request.getLanguageId() + ", testcases count = " + testcases.size());
        Submission submission = Submission.builder()
                .userId(userId)
                .questionId(request.getQuestionId())
                .languageId(request.getLanguageId())
                .sourceCode(request.getSourceCode())
                .status(SubmissionStatus.pending)
                .totalTestcases(testcases.size())
                .score(0)
                .build();
        submission = submissionRepository.save(submission);

        int judgeLang = mapLanguageId(request.getLanguageId());
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);
        for (TestcaseDTO tc : testcases) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("language_id", judgeLang);
            payload.put("source_code", request.getSourceCode());
            payload.put("stdin", tc.getInput() == null ? "" : tc.getInput());
            payload.put("expected_output", tc.getExpectedOutput() == null ? "" : tc.getExpectedOutput());
            // Include callback URL so Judge0 can push results (fallback polling also active)
            String callbackUrl = String.format("%s/api/submissions/callback?submissionId=%s&testCaseId=%s", callbackBaseUrl, submission.getId(), tc.getId());
            payload.put("callback_url", callbackUrl);
            String token = judge0Client.createSubmission(payload);
            if (token != null) {
                tokenRepository.save(SubmissionToken.builder()
                        .token(token)
                        .submissionId(submission.getId())
                        .testcaseId(tc.getId())
                        .status(SubmissionTokenStatus.pending)
                        .expiresAt(expiresAt)
                        .build());
            } else {
                log.error("Judge0 submission creation failed for testcase {}", tc.getId());
            }
        }
        return toResponse(submission);
    }

    // One-off run code without persisting submission/testcases
    public RunCodeResponse runCode(RunCodeRequest request) {
        Map<String,Object> payload = new HashMap<>();
        payload.put("language_id", mapLanguageId(request.getLanguageId()));
        payload.put("source_code", request.getSourceCode());
        payload.put("stdin", request.getStdin()==null?"":request.getStdin());
        Map<String,Object> result = judge0Client.runCode(payload);
        if(result == null) return RunCodeResponse.builder().status("ERROR").message("Judge0 run failed").build();
        Map<String,Object> statusObj = (Map<String,Object>) result.get("status");
        Integer statusId = statusObj!=null && statusObj.get("id")!=null ? ((Number)statusObj.get("id")).intValue(): null;
        String statusDesc = statusObj!=null? String.valueOf(statusObj.get("description")) : null;
        return RunCodeResponse.builder()
                .token((String) result.get("token"))
                .stdout((String) result.get("stdout"))
                .stderr((String) result.get("stderr"))
                .compileOutput((String) result.get("compile_output"))
                .message((String) result.get("message"))
                .status(statusDesc)
                .statusId(statusId)
                .time(result.get("time")!=null? result.get("time").toString():null)
                .memory(result.get("memory")!=null? result.get("memory").toString():null)
                .build();
    }

    public SubmissionResponse getSubmission(String submissionId) {
        return submissionRepository.findById(submissionId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
    }

    public List<SubmissionResponse> listUserSubmissions(String userId) {
        return submissionRepository.findByUserIdOrderBySubmissionTimeDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SubmissionResultResponse> getSubmissionResults(String submissionId) {
        if (!submissionRepository.existsById(submissionId)) {
            throw new NotFoundException("Submission not found");
        }
        return resultRepository.findBySubmissionId(submissionId).stream()
                .map(r -> SubmissionResultResponse.builder()
                        .id(r.getId())
                        .submissionId(r.getSubmissionId())
                        .testcaseId(r.getTestcaseId())
                        .status(r.getStatus())
                        .executionTime(r.getExecutionTime())
                        .memoryUsed(r.getMemoryUsed())
                        .output(r.getOutput())
                        .errorMessage(r.getErrorMessage())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void processTokenResult(SubmissionToken token, Map<String, Object> judge0Data) {
        if (judge0Data == null) return;
        Map<String, Object> statusObj = (Map<String, Object>) judge0Data.get("status");
        int statusId = statusObj != null && statusObj.get("id") != null ? ((Number) statusObj.get("id")).intValue() : -1;

        switch (statusId) {
            case 1, 2 -> token.setStatus(SubmissionTokenStatus.processing); // In queue / Processing
            case 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 -> token.setStatus(SubmissionTokenStatus.completed); // Completed states
            default -> token.setStatus(SubmissionTokenStatus.completed);
        }
        tokenRepository.save(token);

        if (token.getStatus() == SubmissionTokenStatus.completed) {
            SubmissionResultStatus resultStatus = mapResultStatus(statusId);
            BigDecimal time = judge0Data.get("time") != null ? new BigDecimal(judge0Data.get("time").toString()) : null;
            BigDecimal memory = judge0Data.get("memory") != null ? new BigDecimal(judge0Data.get("memory").toString()) : null;
            String stdout = (String) judge0Data.get("stdout");
            String stderr = (String) judge0Data.get("stderr");
            resultRepository.save(SubmissionResult.builder()
                    .submissionId(token.getSubmissionId())
                    .testcaseId(token.getTestcaseId())
                    .status(resultStatus)
                    .executionTime(time)
                    .memoryUsed(memory)
                    .output(stdout)
                    .errorMessage(stderr)
                    .build());
            updateAggregateSubmission(token.getSubmissionId());
        }
    }

    @Transactional
    public void processCallback(JudgeCallback callback){
        if(callback.getSubmissionId()==null || callback.getTestCaseId()==null) return;
        // Idempotency: check existing result
        boolean already = resultRepository.findFirstBySubmissionIdAndTestcaseId(callback.getSubmissionId(), callback.getTestCaseId()).isPresent();
        if(already) return;
        Optional<SubmissionToken> tokenOpt = tokenRepository.findFirstBySubmissionIdAndTestcaseId(callback.getSubmissionId(), callback.getTestCaseId());
        if(tokenOpt.isEmpty()) return;
        SubmissionToken token = tokenOpt.get();
        token.setStatus(SubmissionTokenStatus.completed);
        tokenRepository.save(token);

        int statusId = callback.getStatusId()!=null? callback.getStatusId(): -1;
        SubmissionResultStatus status = mapResultStatus(statusId);
        BigDecimal time = callback.getTime()!=null? safeBigDecimal(callback.getTime()): null;
        BigDecimal memory = callback.getMemory()!=null? safeBigDecimal(callback.getMemory()): null;
        resultRepository.save(SubmissionResult.builder()
                .submissionId(callback.getSubmissionId())
                .testcaseId(callback.getTestCaseId())
                .status(status)
                .executionTime(time)
                .memoryUsed(memory)
                .output(callback.getStdout())
                .errorMessage(firstNonNull(callback.getStderr(), callback.getCompileOutput(), callback.getMessage()))
                .build());
        updateAggregateSubmission(callback.getSubmissionId());
    }

    private BigDecimal safeBigDecimal(String raw){
        try { return new BigDecimal(raw); } catch (Exception e){ return null; }
    }

    private String firstNonNull(String... vals){
        for(String v: vals){ if(v!=null && !v.isBlank()) return v; }
        return null;
    }

    @Transactional
    protected void updateAggregateSubmission(String submissionId) {
        Submission submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) return;
        List<SubmissionResult> results = resultRepository.findBySubmissionId(submissionId);
        int passed = (int) results.stream().filter(r -> r.getStatus() == SubmissionResultStatus.accepted).count();
        submission.setTestcasesPassed(passed);
        submission.setTestcasesFailed(results.size() - passed);

        if (results.size() == submission.getTotalTestcases()) { // All done
            if (passed == submission.getTotalTestcases()) {
                submission.setStatus(SubmissionStatus.accepted);
            } else if (results.stream().anyMatch(r -> r.getStatus() == SubmissionResultStatus.time_limit_exceeded)) {
                submission.setStatus(SubmissionStatus.time_limit_exceeded);
            } else if (results.stream().anyMatch(r -> r.getStatus() == SubmissionResultStatus.memory_limit_exceeded)) {
                submission.setStatus(SubmissionStatus.memory_limit_exceeded);
            } else if (results.stream().anyMatch(r -> r.getStatus() == SubmissionResultStatus.runtime_error)) {
                submission.setStatus(SubmissionStatus.runtime_error);
            } else {
                submission.setStatus(SubmissionStatus.wrong_answer);
            }
        } else {
            // Still running
            submission.setStatus(SubmissionStatus.running);
        }

        submission.setExecutionTime(results.stream()
                .map(SubmissionResult::getExecutionTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null));
        submission.setMemoryUsed(results.stream()
                .map(SubmissionResult::getMemoryUsed)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null));
        submissionRepository.save(submission);
    }

    private SubmissionResultStatus mapResultStatus(int judge0StatusId) {
        return switch (judge0StatusId) {
            case 3 -> SubmissionResultStatus.accepted;            // Accepted
            case 4 -> SubmissionResultStatus.wrong_answer;        // Wrong Answer
            case 5 -> SubmissionResultStatus.time_limit_exceeded; // Time Limit
            case 9 -> SubmissionResultStatus.memory_limit_exceeded; // Memory Limit (heuristic mapping)
            default -> SubmissionResultStatus.runtime_error;      // Compilation / Runtime / Others treated as runtime_error
        };
    }

    private SubmissionResponse toResponse(Submission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .questionId(s.getQuestionId())
                .userId(s.getUserId())
                .languageId(s.getLanguageId())
                .status(s.getStatus())
                .testcasesPassed(s.getTestcasesPassed())
                .testcasesFailed(s.getTestcasesFailed())
                .totalTestcases(s.getTotalTestcases())
                .executionTime(s.getExecutionTime())
                .memoryUsed(s.getMemoryUsed())
                .score(s.getScore())
                .submissionTime(s.getSubmissionTime())
                .build();
    }
}
