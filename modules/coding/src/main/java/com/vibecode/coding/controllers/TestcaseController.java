package com.vibecode.coding.controllers;

import com.vibecode.coding.entity.Testcase;
import com.vibecode.coding.service.TestcaseService;
import com.vibecode.coding.dto.TestcaseDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestcaseController {
    private final TestcaseService testcaseService;

    @GetMapping("/questions/{questionId}/testcases")
    public List<TestcaseDtos.Response> list(@PathVariable String questionId,
                                            @RequestParam(defaultValue = "false") boolean includeHidden,
                                            Authentication auth) {
        boolean isAdmin = hasRole(auth, "ROLE_ADMIN") || hasRole(auth, "admin") || hasRole(auth, "ADMIN");
        boolean allowHidden = isAdmin && includeHidden;
        return testcaseService.list(questionId, allowHidden).stream()
                .map(tc -> map(tc, !isAdmin && Boolean.TRUE.equals(tc.getHidden())))
                .collect(Collectors.toList());
    }

    @GetMapping("/testcases/{id}")
    public TestcaseDtos.Response get(@PathVariable String id, Authentication auth) {
        boolean isAdmin = hasRole(auth, "ROLE_ADMIN") || hasRole(auth, "admin") || hasRole(auth, "ADMIN");
        Testcase tc = testcaseService.get(id);
        return map(tc, !isAdmin && Boolean.TRUE.equals(tc.getHidden()));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','ROLE_ADMIN','admin')")
    @PostMapping("/questions/{questionId}/testcases")
    public ResponseEntity<TestcaseDtos.Response> create(@PathVariable String questionId,
                                                        @Valid @RequestBody TestcaseDtos.Create req) {
        Testcase created = testcaseService.create(questionId, Testcase.builder()
                .input(req.input())
                .expectedOutput(req.expectedOutput())
                .hidden(Boolean.TRUE.equals(req.hidden()))
                .timeLimit(req.timeLimit())
                .memoryLimit(req.memoryLimit())
                .build());
        return ResponseEntity.ok(map(created, false));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','ROLE_ADMIN','admin')")
    @PutMapping("/testcases/{id}")
    public TestcaseDtos.Response update(@PathVariable String id, @Valid @RequestBody TestcaseDtos.Update req) {
        Testcase updated = testcaseService.update(id, Testcase.builder()
                .input(req.input())
                .expectedOutput(req.expectedOutput())
                .hidden(req.hidden())
                .timeLimit(req.timeLimit())
                .memoryLimit(req.memoryLimit())
                .build());
        return map(updated, false);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','ROLE_ADMIN','admin')")
    @DeleteMapping("/testcases/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        testcaseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean hasRole(Authentication auth, String role) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equalsIgnoreCase(role));
    }

    private TestcaseDtos.Response map(Testcase tc, boolean maskOutput) {
        String output = maskOutput ? null : tc.getExpectedOutput();
        return new TestcaseDtos.Response(
                tc.getId(),
                tc.getQuestion().getId(),
                tc.getInput(),
                output,
                Boolean.TRUE.equals(tc.getHidden()),
                tc.getTimeLimit(),
                tc.getMemoryLimit()
        );
    }
}

