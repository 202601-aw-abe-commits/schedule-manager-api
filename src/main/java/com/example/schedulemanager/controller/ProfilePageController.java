package com.example.schedulemanager.controller;

import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.service.LabelColorService;
import com.example.schedulemanager.service.UserAccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfilePageController {
    private final UserAccountService userAccountService;
    private final LabelColorService labelColorService;

    public ProfilePageController(UserAccountService userAccountService, LabelColorService labelColorService) {
        this.userAccountService = userAccountService;
        this.labelColorService = labelColorService;
    }

    @GetMapping("/profile")
    public String profile(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestParam(value = "mode", required = false) String mode) {
        AppUser user = userAccountService.getByUsername(userDetails.getUsername());
        boolean editMode = "edit".equalsIgnoreCase(mode);
        model.addAttribute("currentUsername", user.getUsername());
        model.addAttribute("currentDisplayName", user.getDisplayName());
        model.addAttribute("currentEmail", user.getEmail());
        model.addAttribute("currentProfileBio", user.getProfileBio());
        model.addAttribute("currentXUrl", user.getXUrl());
        model.addAttribute("currentStreamUrl", user.getStreamUrl());
        model.addAttribute("hasProfileImage", user.getProfileImageData() != null && user.getProfileImageData().length > 0);
        model.addAttribute("editMode", editMode);
        model.addAttribute("labelColorStyle", labelColorService.toInlineStyle(user.getId()));
        return "profile";
    }
}
