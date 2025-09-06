package com.vibecode.coding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "questions", schema = "videcode")
public class Question {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "input_format", columnDefinition = "TEXT")
    private String inputFormat;

    @Column(name = "output_format", nullable = false, columnDefinition = "TEXT")
    private String outputFormat;

    @Column(name = "constraints", nullable = false, columnDefinition = "TEXT")
    private String constraints;

    @Column(name = "sample_test_input", columnDefinition = "TEXT")
    private String sampleTestInput;

    @Column(name = "sample_test_output", columnDefinition = "TEXT")
    private String sampleTestOutput;

    @Convert(converter = com.vibecode.coding.entity.converter.DifficultyConverter.class)
    @Column(name = "difficulty", nullable = false, columnDefinition = "ENUM('easy','medium','hard')")
    private Difficulty difficulty;

    @Column(name = "time_limit", nullable = false, precision = 5, scale = 2)
    private BigDecimal timeLimit;

    @Column(name = "memory_limit", nullable = false, precision = 8, scale = 2)
    private BigDecimal memoryLimit;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}

