package com.example.schedulemanager.security;

import com.example.schedulemanager.service.GamificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(LoginSuccessHandler.class);

    private final GamificationService gamificationService;

    public LoginSuccessHandler(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        try {
            gamificationService.awardDailyLogin(authentication.getName());
        } catch (Exception ex) {
            log.warn("Failed to award daily login points for user={}", authentication.getName(), ex);
        }
        response.sendRedirect("/calendar");
    }
}
