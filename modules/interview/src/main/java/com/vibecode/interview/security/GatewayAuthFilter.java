package com.vibecode.interview.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GatewayAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String rolesHeader = request.getHeader("X-User-Roles");

        System.out.println("=== Gateway Filter Debug ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Gateway headers - UserId: " + userId + ", Username: " + username + ", Roles: " + rolesHeader);

        if (userId != null && username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<String> roles = rolesHeader != null
                    ? Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .toList()
                    : List.of();

            UserPrincipal principal = new UserPrincipal(userId, username, roles);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            System.out.println("Authentication set successfully!");
            System.out.println("Principal: " + authToken.getPrincipal());
            System.out.println("Authorities: " + authToken.getAuthorities());
            System.out.println("Is Authenticated: " + authToken.isAuthenticated());
        } else {
            System.out.println("Authentication NOT set - reasons:");
            System.out.println("UserId null: " + (userId == null));
            System.out.println("Username null: " + (username == null));
            System.out.println("Existing auth: " + (SecurityContextHolder.getContext().getAuthentication() != null));
        }

        // Check authentication before proceeding
        System.out.println("Before filterChain - Current Authentication: " +
                SecurityContextHolder.getContext().getAuthentication());

        filterChain.doFilter(request, response);

        System.out.println("After filterChain - Response Status: " + response.getStatus());
        System.out.println("=== End Gateway Filter Debug ===");
    }

}
