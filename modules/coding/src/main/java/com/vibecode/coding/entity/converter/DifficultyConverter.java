package com.vibecode.coding.entity.converter;

import com.vibecode.coding.entity.Question.Difficulty;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DifficultyConverter implements AttributeConverter<Difficulty, String> {
    @Override
    public String convertToDatabaseColumn(Difficulty attribute) {
        if (attribute == null) return null;
        return switch (attribute) {
            case EASY -> "easy";
            case MEDIUM -> "medium";
            case HARD -> "hard";
        };
    }

    @Override
    public Difficulty convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return switch (dbData.toLowerCase()) {
            case "easy" -> Difficulty.EASY;
            case "hard" -> Difficulty.HARD;
            default -> Difficulty.MEDIUM;
        };
    }
}

