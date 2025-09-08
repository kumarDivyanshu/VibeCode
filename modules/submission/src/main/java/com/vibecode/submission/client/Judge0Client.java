package com.vibecode.submission.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class Judge0Client {

    private final RestTemplate restTemplate;

    @Value("${judge0.base-url:https://judge0-ce.p.rapidapi.com}")
    private String baseUrl;

    @Value("${judge0.wait-parameter:false}")
    private boolean waitParam;

    @Value("${judge0.use-rapidapi:false}")
    private boolean useRapidApi;

    // Fixed malformed placeholder & removed hard-coded key
    @Value("${judge0.rapidapi.key:}")
    private String rapidApiKey;

    @Value("${judge0.rapidapi.host:judge0-ce.p.rapidapi.com}")
    private String rapidApiHost;

    @Value("${submission.run.max-poll:8}")
    private int runMaxPoll;

    @Value("${submission.run.poll-delay-ms:400}")
    private long runPollDelayMs;

    public String createSubmission(Map<String, Object> payload) {
        return createSubmissionInternal(payload, false) instanceof String s ? s : null;
    }

    public Map<String, Object> fetchSubmission(String token) {
        String url = baseUrl + "/submissions/" + token + "?base64_encoded=false&fields=*";
        HttpHeaders headers = buildHeaders();
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                log.debug("Judge0 fetch token={} status={} statusText={} done", token, resp.getStatusCodeValue(), extractStatusDesc(resp.getBody()));
                return resp.getBody();
            } else {
                log.warn("Judge0 fetch token={} non-2xx status={} body={}", token, resp.getStatusCodeValue(), resp.getBody());
            }
        } catch (Exception ex) {
            log.error("Judge0 fetch error token {}: {}", token, ex.getMessage(), ex);
        }
        return null;
    }

    public Map<String, Object> runCode(Map<String, Object> payload) {
        log.debug("runCode: start payloadKeys={} lang={} wait=true", payload.keySet(), payload.get("language_id"));
        Object result = createSubmissionInternal(payload, true);
        if (!(result instanceof Map<?, ?> map)) {
            log.error("runCode: initial Judge0 response null or invalid");
            return null;
        }
        //noinspection unchecked
        Map<String,Object> res = (Map<String,Object>) map;
        Integer statusId = extractStatusId(res);
        String token = (String) res.get("token");
        if (token == null) {
            log.warn("runCode: missing token in initial response res={}", res);
            return res; // return what we have
        }
        int attempts = 0;
        while (statusId != null && (statusId == 1 || statusId == 2) && attempts < runMaxPoll) { // In Queue / Processing
            attempts++;
            try { Thread.sleep(runPollDelayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            Map<String,Object> fetched = fetchSubmission(token);
            if (fetched != null) {
                res = fetched;
                statusId = extractStatusId(res);
                log.debug("runCode polling attempt={} token={} statusId={} desc={}", attempts, token, statusId, extractStatusDesc(res));
                if (!(statusId == 1 || statusId == 2)) break;
            } else {
                log.warn("runCode polling attempt={} token={} returned null", attempts, token);
            }
        }
        log.debug("runCode: completed token={} finalStatusId={} desc={} attempts={}", token, statusId, extractStatusDesc(res), attempts);
        return res;
    }

    private Object createSubmissionInternal(Map<String, Object> payload, boolean wait) {
        String url = baseUrl + "/submissions?base64_encoded=false&fields=*&wait=" + wait;
        HttpHeaders headers = buildHeaders();
        log.debug("Judge0 submit wait={} url={} useRapidApi={} headers={} payloadKeys={}", wait, url, useRapidApi, headerNames(headers), payload.keySet());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                log.debug("Judge0 submit success wait={} status={} token={} statusDesc={}", wait, resp.getStatusCodeValue(), resp.getBody().get("token"), extractStatusDesc(resp.getBody()));
                if (wait) return resp.getBody();
                Object token = resp.getBody().get("token");
                return token == null ? null : token.toString();
            } else {
                log.warn("Judge0 submit failure status={} body={}", resp.getStatusCodeValue(), resp.getBody());
            }
        } catch (Exception ex) {
            log.error("Judge0 submission error wait={} message={} payloadKeys={}", wait, ex.getMessage(), payload.keySet(), ex);
        }
        return null;
    }

    private HttpHeaders buildHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (useRapidApi && rapidApiKey != null && !rapidApiKey.isBlank()) {
            headers.add("X-RapidAPI-Key", rapidApiKey);
            headers.add("X-RapidAPI-Host", rapidApiHost);
        }
        return headers;
    }

    private Integer extractStatusId(Map<String,Object> body){
        if(body == null) return null;
        Object statusObj = body.get("status");
        if(statusObj instanceof Map<?,?> map){
            Object id = map.get("id");
            if(id instanceof Number n) return n.intValue();
        }
        Object direct = body.get("status_id");
        if(direct instanceof Number n) return n.intValue();
        return null;
    }

    private String extractStatusDesc(Map<String,Object> body){
        if(body == null) return null;
        Object statusObj = body.get("status");
        if(statusObj instanceof Map<?,?> map){
            Object d = map.get("description");
            return d==null? null: d.toString();
        }
        return null;
    }

    private Set<String> headerNames(HttpHeaders headers){
        return headers.keySet();
    }
}
