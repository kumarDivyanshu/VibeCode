package com.vibecode.coding.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "testcases", schema = "videcode")
public class Testcase {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "input", nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(name = "expected_output", nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(name = "is_hidden", nullable = false)
    private Boolean hidden;

    @Column(name = "time_limit", nullable = false, precision = 5, scale = 2)
    private BigDecimal timeLimit;

    @Column(name = "memory_limit", nullable = false, precision = 8, scale = 2)
    private BigDecimal memoryLimit;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

