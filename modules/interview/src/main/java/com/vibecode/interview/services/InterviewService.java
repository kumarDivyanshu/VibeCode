package com.vibecode.interview.services;

import com.vibecode.interview.dto.CreateInterviewRequest;
import com.vibecode.interview.dto.InterviewResponse;
import com.vibecode.interview.dto.UpdateInterviewRequest;
import com.vibecode.interview.models.Interview;
import com.vibecode.interview.models.InterviewStatus;
import com.vibecode.interview.repositories.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;

    @Transactional
    public InterviewResponse create(String userId, CreateInterviewRequest req) {
        Interview interview = Interview.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .companyId(req.companyId().toString())
                .description(req.description())
                .scheduledDate(req.scheduledDate())
                .status(InterviewStatus.completed)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Interview saved = interviewRepository.save(interview);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InterviewResponse getById(String userId, String id) {
        Interview interview = interviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));
        return toResponse(interview);
    }

    @Transactional(readOnly = true)
    public Page<InterviewResponse> list(String userId,
                                        Optional<InterviewStatus> status,
                                        Optional<UUID> companyId,
                                        Optional<LocalDateTime> from,
                                        Optional<LocalDateTime> to,
                                        int page,
                                        int size,
                                        Optional<String> sortBy,
                                        Optional<Sort.Direction> direction) {
        Sort sort = Sort.by(direction.orElse(Sort.Direction.DESC), sortBy.orElse("createdAt"));
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);

        Specification<Interview> spec = userScope(userId);
        if (status.isPresent()) spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status.get()));
        if (companyId.isPresent()) spec = spec.and((root, q, cb) -> cb.equal(root.get("companyId"), companyId.get()));
        if (from.isPresent() && to.isPresent()) {
            spec = spec.and((root, q, cb) -> cb.between(root.get("scheduledDate"), from.get(), to.get()));
        } else if (from.isPresent()) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("scheduledDate"), from.get()));
        } else if (to.isPresent()) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("scheduledDate"), to.get()));
        }

        return interviewRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public InterviewResponse update(String userId, String id, UpdateInterviewRequest req) {
        Interview interview = interviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));

        if (req.description() != null) interview.setDescription(req.description());
        if (req.scheduledDate() != null) interview.setScheduledDate(req.scheduledDate());
        if (req.status() != null) interview.setStatus(req.status());
        if (req.score() != null) interview.setScore(req.score());
        if (req.feedback() != null) interview.setFeedback(req.feedback());

        Interview saved = interviewRepository.save(interview);
        return toResponse(saved);
    }

    @Transactional
    public void delete(String userId, String id) {
        Interview interview = interviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));
        interviewRepository.delete(interview);
    }

    private Specification<Interview> userScope(String userId) {
        return (root, q, cb) -> cb.equal(root.get("userId"), userId);
    }

    private InterviewResponse toResponse(Interview i) {
        return new InterviewResponse(
                i.getId(),
                i.getUserId(),
                i.getCompanyId(),
                i.getDescription(),
                i.getScheduledDate(),
                i.getStatus(),
                i.getScore(),
                i.getFeedback(),
                i.getCreatedAt(),
                i.getUpdatedAt()
        );
    }
}
