package com.vibecode.gate.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Log incoming request
        logger.info("Incoming request: {} {} from {}",
                request.getMethod(),
                request.getURI(),
                getClientIp(request));

        // Log request headers (excluding sensitive data)
        request.getHeaders().forEach((name, values) -> {
            if (!name.toLowerCase().contains("authorization")) {
                logger.debug("Request header: {} = {}", name, values);
            }
        });

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).then(
            Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                logger.info("Response: {} {} - Status: {} - Duration: {}ms",
                        request.getMethod(),
                        request.getURI(),
                        response.getStatusCode(),
                        duration);
            })
        );
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ?
               request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return -1; // Execute this filter first
    }
}
