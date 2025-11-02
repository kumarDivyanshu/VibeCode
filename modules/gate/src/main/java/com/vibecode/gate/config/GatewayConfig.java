package com.vibecode.gate.config;

import com.vibecode.gate.filter.AuthenticationFilter;
import com.vibecode.gate.filter.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Handle CORS preflight globally - short-circuit with 200 OK
                .route("cors-preflight", r -> r.method(HttpMethod.OPTIONS).and().path("/**")
                        .filters(f -> f
                                .setResponseHeader("Access-Control-Allow-Origin", "http://localhost:3000")
                                .setResponseHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS")
                                .setResponseHeader("Access-Control-Allow-Headers", "Authorization,Content-Type,X-Requested-With,Accept,Origin,X-User-Id,X-Username,X-User-Roles")
                                .setResponseHeader("Access-Control-Allow-Credentials", "true")
                                .setStatus(HttpStatus.OK))
                        .uri("no://op"))

                // Public read access for interviews - GET only, no auth filter
                .route("interview-service-public", r -> r.method(HttpMethod.GET).and().path("/interviews/**")
                        .filters(f -> f
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8082"))

                // Auth service routes - no authentication required but with rate limiting
                .route("auth-login", r -> r.path("/auth/login")
                        .filters(f -> f.filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8081"))

                .route("auth-register", r -> r.path("/auth/register")
                        .filters(f -> f.filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8081"))

                .route("auth-refresh", r -> r.path("/auth/refresh")
                        .filters(f -> f.filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8081"))

                // Protected auth routes
                .route("auth-protected", r -> r.path("/auth/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8081"))

                // Interview service routes - all protected for non-GET
                .route("interview-service", r -> r.path("/interviews/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8082"))

                // Optional: Add backward compatibility route for /interview (singular)
                .route("interview-service-singular", r -> r.path("/interview/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config()))
                                .rewritePath("/interview/(?<path>.*)", "/interviews/${path}"))
                        .uri("http://localhost:8082"))

                // Coding service routes
                .route("questions-service", r -> r.path("/questions/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8083"))
                .route("testcases-service", r -> r.path("/testcases/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8083"))
                // NEW: Sheets routes to coding service
                .route("sheets-service", r -> r.path("/sheets/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8083"))

                // Submission service routes
                .route("submission-open", r -> r.path("/api/submissions/callback", "/api/submissions/runcode")
                        .filters(f -> f
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8092"))
                .route("submission-service", r -> r.path("/api/submissions/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8092"))

                // AiHelper service routes - all protected
                .route("aiHelper-service", r -> r.path("/ai/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8090"))

                // Health check routes - no authentication required
                .route("auth-health", r -> r.path("/actuator/health")
                        .and().header("X-Service", "auth")
                        .uri("http://localhost:8081"))
                .route("interview-health", r -> r.path("/actuator/health")
                        .and().header("X-Service", "interview")
                        .uri("http://localhost:8082"))

                // Gateway health and routes endpoint
                .route("gateway-info", r -> r.path("/api/gateway/**")
                        .uri("http://localhost:8080"))

                .build();
    }
}