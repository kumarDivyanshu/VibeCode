package com.vibecode.submission.client;

import com.vibecode.submission.dto.TestcaseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestcaseServiceClient {

    private final RestTemplate restTemplate;

    // Base URL of coding service (hosting /testcases controller)
    @Value("${services.coding.base-url:http://localhost:8080}")
    private String codingBaseUrl;

    public List<TestcaseDTO> getTestcasesForQuestion(String questionId) {
        try {
            // Use internal flag to retrieve all (including hidden) testcases with expected outputs
            String url = codingBaseUrl + "/testcases/question/" + questionId;
            System.out.println("URL = " + url);
            ResponseEntity<List<TestcaseDTO>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return resp.getBody() == null ? Collections.emptyList() : resp.getBody();
        } catch (Exception ex) {
            log.error("Failed fetching testcases for question {}: {}", questionId, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
