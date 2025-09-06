package com.vibecode.interview.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews", schema = "videcode")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "company_id", length = 36, nullable = false)
    private String companyId;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "scheduled_date", nullable = true)
    private LocalDateTime scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InterviewStatus status;

    @Column(name = "score")
    private Integer score;

    @Lob
    @Column(name = "feedback")
    private String feedback;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
