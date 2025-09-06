package com.vibecode.interview.controllers;

import com.vibecode.interview.dto.CreateInterviewRequest;
import com.vibecode.interview.dto.InterviewResponse;
import com.vibecode.interview.dto.UpdateInterviewRequest;
import com.vibecode.interview.models.InterviewStatus;
import com.vibecode.interview.security.UserPrincipal;
import com.vibecode.interview.services.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewResponse create(@AuthenticationPrincipal UserPrincipal principal,
                                    @RequestBody CreateInterviewRequest request) {
        System.out.println(principal.getId());
        return interviewService.create(principal.getId(), request);
    }

    @GetMapping("/{id}")
    public InterviewResponse getById(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable String id) {
        return interviewService.getById(principal.getId(), id);
    }

    @GetMapping
    public Page<InterviewResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                        @RequestParam Optional<InterviewStatus> status,
                                        @RequestParam Optional<UUID> companyId,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> from,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> to,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam Optional<String> sortBy,
                                        @RequestParam Optional<Sort.Direction> direction) {
        return interviewService.list(principal.getId(), status, companyId, from, to, page, size, sortBy, direction);
    }

    @PatchMapping("/{id}")
    public InterviewResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable String id,
                                    @Valid @RequestBody UpdateInterviewRequest request) {
        return interviewService.update(principal.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal,
                       @PathVariable String id) {
        interviewService.delete(principal.getId(), id);
    }
}
