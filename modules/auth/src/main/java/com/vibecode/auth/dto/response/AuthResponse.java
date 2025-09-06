package com.vibecode.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private String id;
    private String email;
    private String name;
    private boolean banned;
}

