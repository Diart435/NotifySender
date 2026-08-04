package com.notify.api.filter;

import com.notify.api.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyValidationFilter implements Filter {
    private final UserService userService;
    private final BCryptPasswordEncoder encoder;
    @Value("${admin.api.key}")
    private String adminApiKey;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String apiKey = httpRequest.getHeader("X-API-Key");
        boolean isValid = adminApiKey.equals(apiKey) || userService.isApiKey(encoder.encode(apiKey));
        if(!isValid){
            sendUnauthorized(response, "API-key doesn't exist");
            return;
        }
        log.debug("Вход разрешен");
        chain.doFilter(request, response);
    }
    private void sendUnauthorized(ServletResponse response, String message) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getWriter().write(message);
    }
}
