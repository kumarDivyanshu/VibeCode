package com.vibecode.aihelper.modals.converters;

import com.vibecode.aihelper.modals.EDifficulty;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DifficultyConverter implements AttributeConverter<EDifficulty, String> {
    @Override
    public String convertToDatabaseColumn(EDifficulty attribute) {
        if (attribute == null) return null;
        return switch (attribute) {
            case EASY -> "easy";
            case MEDIUM -> "medium";
            case HARD -> "hard";
        };
    }

    @Override
    public EDifficulty convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return switch (dbData.toLowerCase()) {
            case "easy" -> EDifficulty.EASY;
            case "hard" -> EDifficulty.HARD;
            default -> EDifficulty.MEDIUM;
        };
    }
}

