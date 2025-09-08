package com.vibecode.submission.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        RestTemplate rt = new RestTemplate(new BufferingClientHttpRequestFactory(factory));

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new AuthorizationHeaderPropagatingInterceptor());
        // You can add logging interceptor here later if needed
        rt.setInterceptors(interceptors);
        return rt;
    }

    static class AuthorizationHeaderPropagatingInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(org.springframework.http.HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                HttpServletRequest servletRequest = servletAttrs.getRequest();
                String auth = servletRequest.getHeader("Authorization");
                if (auth != null && !auth.isBlank() && !request.getHeaders().containsKey("Authorization")) {
                    request.getHeaders().add("Authorization", auth);
                }
            }
            return execution.execute(request, body);
        }
    }
}
