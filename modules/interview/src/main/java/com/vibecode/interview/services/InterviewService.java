package com.vibecode.interview.services;

import com.vibecode.interview.dto.CreateInterviewRequest;
import com.vibecode.interview.dto.InterviewResponse;
import com.vibecode.interview.dto.UpdateInterviewRequest;
import com.vibecode.interview.models.Interview;
import com.vibecode.interview.models.InterviewStatus;
import com.vibecode.interview.models.Company; // NEW
import com.vibecode.interview.models.User; // NEW
import com.vibecode.interview.repositories.InterviewRepository;
import com.vibecode.interview.repositories.CompanyRepository; // NEW
import com.vibecode.interview.repositories.UserRepository; // NEW
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
    private final CompanyRepository companyRepository; // CHANGED
    private final UserRepository userRepository;       // NEW

    @Transactional
    public InterviewResponse create(String userId, String userEmail, CreateInterviewRequest req) {
        // Resolve or create company by name
        String normalizedName = req.companyName().trim();
        Company company = companyRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> companyRepository.save(
                        Company.builder()
                                .id(UUID.randomUUID().toString())
                                .name(normalizedName)
                                .build()
                ));

        InterviewStatus status = (req.status() != null) ? req.status() : InterviewStatus.scheduled;
        Interview interview = Interview.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .companyId(company.getId())
                .description(req.description())
                .scheduledDate(req.scheduledDate())
                .status(status)
                .score(req.score())
                .feedback(req.feedback())
                // createdAt/updatedAt managed by DB
                .build();
        Interview saved = interviewRepository.save(interview);
        return toResponse(saved, userEmail);
    }

    // --- Public READ methods (no authentication required) ---
    @Transactional(readOnly = true)
    public InterviewResponse getById(String id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));
        return toResponse(interview); // compute names from DB only
    }

    @Transactional(readOnly = true)
    public Page<InterviewResponse> list(Optional<InterviewStatus> status,
                                        Optional<UUID> companyId,
                                        Optional<LocalDateTime> from,
                                        Optional<LocalDateTime> to,
                                        int page,
                                        int size,
                                        Optional<String> sortBy,
                                        Optional<Sort.Direction> direction) {
        Sort sort = Sort.by(direction.orElse(Sort.Direction.DESC), sortBy.orElse("createdAt"));
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);

        Specification<Interview> spec = (root, q, cb) -> cb.conjunction();
        if (status.isPresent()) spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status.get()));
        if (companyId.isPresent()) spec = spec.and((root, q, cb) -> cb.equal(root.get("companyId"), companyId.get().toString()));
        if (from.isPresent() && to.isPresent()) {
            spec = spec.and((root, q, cb) -> cb.between(root.get("scheduledDate"), from.get(), to.get()));
        } else if (from.isPresent()) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("scheduledDate"), from.get()));
        } else if (to.isPresent()) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("scheduledDate"), to.get()));
        }

        return interviewRepository.findAll(spec, pageable).map(this::toResponse);
    }

    // NEW: list interviews by company name (public)
    @Transactional(readOnly = true)
    public Page<InterviewResponse> listByCompanyName(String companyName,
                                                     int page,
                                                     int size,
                                                     Optional<String> sortBy,
                                                     Optional<Sort.Direction> direction) {
        Sort sort = Sort.by(direction.orElse(Sort.Direction.DESC), sortBy.orElse("createdAt"));
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);

        Optional<Company> companyOpt = companyRepository.findByNameIgnoreCase(companyName.trim());
        if (companyOpt.isEmpty()) {
            // Return empty page if company not found
            Specification<Interview> emptySpec = (root, q, cb) -> cb.disjunction();
            return interviewRepository.findAll(emptySpec, pageable).map(this::toResponse);
        }
        String companyId = companyOpt.get().getId();
        Specification<Interview> spec = (root, q, cb) -> cb.equal(root.get("companyId"), companyId);
        return interviewRepository.findAll(spec, pageable).map(this::toResponse);
    }

    // NEW: count interviews for a company name (public)
    @Transactional(readOnly = true)
    public long countByCompanyName(String companyName) {
        return companyRepository.findByNameIgnoreCase(companyName.trim())
                .map(c -> interviewRepository.countByCompanyId(c.getId()))
                .orElse(0L);
    }

    // --- Existing authenticated READ method kept for backward compatibility ---
    @Transactional(readOnly = true)
    public InterviewResponse getById(String userId, String userEmail, String id) {
        Interview interview = interviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));
        return toResponse(interview, userEmail);
    }

    @Transactional(readOnly = true)
    public Page<InterviewResponse> list(String userId,
                                        String userEmail,
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
        if (companyId.isPresent()) spec = spec.and((root, q, cb) -> cb.equal(root.get("companyId"), companyId.get().toString()));
        if (from.isPresent() && to.isPresent()) {
            spec = spec.and((root, q, cb) -> cb.between(root.get("scheduledDate"), from.get(), to.get()));
        } else if (from.isPresent()) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("scheduledDate"), from.get()));
        } else if (to.isPresent()) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("scheduledDate"), to.get()));
        }

        return interviewRepository.findAll(spec, pageable).map(i -> toResponse(i, userEmail));
    }

    @Transactional
    public InterviewResponse update(String userId, String userEmail, String id, UpdateInterviewRequest req) {
        Interview interview = interviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));

        if (req.description() != null) interview.setDescription(req.description());
        if (req.scheduledDate() != null) interview.setScheduledDate(req.scheduledDate());
        if (req.status() != null) interview.setStatus(req.status());
        if (req.score() != null) interview.setScore(req.score());
        if (req.feedback() != null) interview.setFeedback(req.feedback());

        Interview saved = interviewRepository.save(interview);
        return toResponse(saved, userEmail);
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

    // Overload for public read: compute names from DB, fallback to ids
    private InterviewResponse toResponse(Interview i) {
        String companyName = null;
        if (i.getCompanyId() != null) {
            companyName = companyRepository.findById(i.getCompanyId())
                    .map(Company::getName)
                    .orElse(i.getCompanyId());
        }
        String userName = null;
        if (i.getUserId() != null) {
            userName = userRepository.findById(i.getUserId())
                    .map(User::getName)
                    .orElse(i.getUserId());
        }
        return new InterviewResponse(
                i.getId(),
                i.getUserId(),
                userName,
                i.getCompanyId(),
                companyName,
                i.getDescription(),
                i.getScheduledDate(),
                i.getStatus(),
                i.getScore(),
                i.getFeedback(),
                i.getCreatedAt(),
                i.getUpdatedAt()
        );
    }

    private InterviewResponse toResponse(Interview i, String userEmail) {
        String companyName = null;
        if (i.getCompanyId() != null) {
            companyName = companyRepository.findById(i.getCompanyId())
                    .map(Company::getName)
                    .orElse(i.getCompanyId());
        }
        String userName = null;
        if (i.getUserId() != null) {
            userName = userRepository.findById(i.getUserId())
                    .map(User::getName)
                    .orElse(userEmail); // fallback to principal email
        }
        return new InterviewResponse(
                i.getId(),
                i.getUserId(),
                userName,
                i.getCompanyId(),
                companyName,
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
