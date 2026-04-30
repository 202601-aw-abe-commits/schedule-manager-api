package com.example.schedulemanager.controller;

import com.example.schedulemanager.dto.RegisterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.example.schedulemanager.service.UserAccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthPageController {
    private final UserAccountService userAccountService;

    public AuthPageController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "registerError", required = false) String registerError,
            Model model) {
        model.addAttribute("loginError", error != null);
        model.addAttribute("loggedOut", logout != null);
        model.addAttribute("registered", registered != null);
        model.addAttribute("registerError", registerError);
        return "login";
    }

    @PostMapping("/register")
    public String register(RegisterRequest registerRequest) {
        try {
            userAccountService.register(registerRequest);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            return "redirect:/login?registerError=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
        }
    }
}
