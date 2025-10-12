package com.vibecode.aihelper.modals.converters;

import com.vibecode.aihelper.modals.ERole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoleConverter implements AttributeConverter<ERole, String> {
    @Override
    public String convertToDatabaseColumn(ERole attribute) {
        if (attribute == null) return null;
        return switch (attribute) {
            case ROLE_USER -> "user";
            case ROLE_ASSISTANT -> "assistant";
        };
    }

    @Override
    public ERole convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return switch (dbData.toLowerCase()) {
            case "user" -> ERole.ROLE_USER;
            case "assistant" -> ERole.ROLE_ASSISTANT;
            default -> ERole.ROLE_USER;
        };
    }
}

