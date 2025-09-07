package com.vibecode.submission.controller;

import com.vibecode.submission.dto.*;
import com.vibecode.submission.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    // In absence of integrated auth context, take user id from header (gateway should inject)
    private String resolveUserId(String headerUserId){
        return headerUserId; // could add validation
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> create(@RequestHeader("X-User-Id") String userId,
                                                     @Valid @RequestBody SubmissionRequest request){
        return ResponseEntity.ok(submissionService.createSubmission(request, resolveUserId(userId)));
    }

    // Alias /submit (more explicit)
    @PostMapping("/submit")
    public ResponseEntity<SubmissionResponse> submit(@RequestHeader("X-User-Id") String userId,
                                                     @Valid @RequestBody SubmissionRequest request){
        return ResponseEntity.ok(submissionService.createSubmission(request, resolveUserId(userId)));
    }

    // Run code ad-hoc (no persistence / aggregation)
    @PostMapping("/runcode")
    public ResponseEntity<RunCodeResponse> runCode(@Valid @RequestBody RunCodeRequest request){
        return ResponseEntity.ok(submissionService.runCode(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> get(@PathVariable String id){
        return ResponseEntity.ok(submissionService.getSubmission(id));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<List<SubmissionResultResponse>> results(@PathVariable String id){
        return ResponseEntity.ok(submissionService.getSubmissionResults(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubmissionResponse>> listForUser(@PathVariable String userId){
        return ResponseEntity.ok(submissionService.listUserSubmissions(userId));
    }

    // Judge0 callback endpoint (callback_url points here with query params)
    @PostMapping("/callback")
    public ResponseEntity<Void> judgeCallback(@RequestParam String submissionId,
                                              @RequestParam String testCaseId,
                                              @RequestBody Map<String,Object> body){
        JudgeCallback cb = new JudgeCallback();
        cb.setSubmissionId(submissionId);
        cb.setTestCaseId(testCaseId);
        Object statusObj = body.get("status");
        if(statusObj instanceof Map<?,?> map){
            Object id = map.get("id");
            if(id instanceof Number n) cb.setStatusId(n.intValue());
            Object desc = map.get("description");
            if(desc!=null) cb.setStatusDescription(desc.toString());
        } else if(body.get("status_id") instanceof Number n){
            cb.setStatusId(n.intValue());
        }
        if(body.get("stdout")!=null) cb.setStdout((String) body.get("stdout"));
        if(body.get("stderr")!=null) cb.setStderr((String) body.get("stderr"));
        if(body.get("compile_output")!=null) cb.setCompileOutput((String) body.get("compile_output"));
        if(body.get("message")!=null) cb.setMessage((String) body.get("message"));
        if(body.get("time")!=null) cb.setTime(String.valueOf(body.get("time")));
        if(body.get("memory")!=null) cb.setMemory(String.valueOf(body.get("memory")));
        submissionService.processCallback(cb);
        return ResponseEntity.ok().build();
    }
}
