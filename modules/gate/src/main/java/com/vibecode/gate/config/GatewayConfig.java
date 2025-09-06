package com.vibecode.gate.config;

import com.vibecode.gate.filter.AuthenticationFilter;
import com.vibecode.gate.filter.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
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

                // Interview service routes - all protected
                // Changed from /interview/** to /interviews/** to match your controller
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

                // Interview service routes - all protected
                // Changed from /question/** to /questions/** to match your controller
                .route("questions-service", r -> r.path("/questions/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8083"))

                // Testcases routes /questions/** to match your controller
                .route("testcases-service", r -> r.path("/testcases/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitFilter.apply(new RateLimitFilter.Config())))
                        .uri("http://localhost:8083"))

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