package com.vibecode.gate.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientId = getClientId(exchange);
            long currentTime = System.currentTimeMillis();

            // Reset counter if window has passed
            Long lastTime = lastRequestTime.get(clientId);
            if (lastTime == null || currentTime - lastTime > config.getWindowSizeInMillis()) {
                requestCounts.put(clientId, new AtomicInteger(0));
                lastRequestTime.put(clientId, currentTime);
            }

            AtomicInteger count = requestCounts.computeIfAbsent(clientId, k -> new AtomicInteger(0));

            if (count.incrementAndGet() > config.getMaxRequests()) {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }

    private String getClientId(org.springframework.web.server.ServerWebExchange exchange) {
        // You can implement more sophisticated client identification here
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null ?
               exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    public static class Config {
        private int maxRequests = 100;
        private long windowSizeInMillis = Duration.ofMinutes(1).toMillis();

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        public long getWindowSizeInMillis() {
            return windowSizeInMillis;
        }

        public void setWindowSizeInMillis(long windowSizeInMillis) {
            this.windowSizeInMillis = windowSizeInMillis;
        }
    }
}
