package com.example.schedulemanager.controller;

import com.example.schedulemanager.dto.ProfileUpdateRequest;
import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.service.UserAccountService;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class UserApiController {
    private final UserAccountService userAccountService;

    public UserApiController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping
    public Map<String, Object> me(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userAccountService.getByUsername(userDetails.getUsername());
        return toMap(user);
    }

    @PutMapping
    public Map<String, Object> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProfileUpdateRequest request) {
        AppUser user = userAccountService.updateProfile(userDetails.getUsername(), request);
        return toMap(user);
    }

    private Map<String, Object> toMap(AppUser user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName(),
                "profileBio", user.getProfileBio() == null ? "" : user.getProfileBio(),
                "profileImageUrl", user.getProfileImageUrl() == null ? "" : user.getProfileImageUrl());
    }
}
