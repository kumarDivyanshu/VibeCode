package com.vibecode.submission.scheduler;

import com.vibecode.submission.client.Judge0Client;
import com.vibecode.submission.entity.SubmissionToken;
import com.vibecode.submission.entity.SubmissionTokenStatus;
import com.vibecode.submission.repository.SubmissionTokenRepository;
import com.vibecode.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionPollingScheduler {

    private final SubmissionTokenRepository tokenRepository;
    private final Judge0Client judge0Client;
    private final SubmissionService submissionService;

    @Scheduled(fixedDelayString = "${submission.poll-interval-ms:5000}")
    public void pollPendingTokens(){
        List<SubmissionToken> tokens = tokenRepository.findByStatusInAndExpiresAtAfter(
                Arrays.asList(SubmissionTokenStatus.pending, SubmissionTokenStatus.processing),
                Instant.now());
        if(tokens.isEmpty()) return;
        for(SubmissionToken token : tokens){
            try {
                Map<String,Object> data = judge0Client.fetchSubmission(token.getToken());
                if(data != null) {
                    submissionService.processTokenResult(token, data);
                }
            } catch (Exception ex){
                log.error("Polling error for token {}: {}", token.getToken(), ex.getMessage());
            }
        }
    }
}

