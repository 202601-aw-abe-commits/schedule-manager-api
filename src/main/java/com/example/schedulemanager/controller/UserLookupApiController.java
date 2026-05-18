package com.example.schedulemanager.controller;

import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.service.UserAccountService;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserLookupApiController {
    private final UserAccountService userAccountService;

    public UserLookupApiController(
            UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/{userId}/profile-image")
    public ResponseEntity<byte[]> profileImage(
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Any authenticated user can view profile images from user lookup pages.
        userAccountService.getByUsername(userDetails.getUsername());

        AppUser targetUser = userAccountService.getById(userId);
        byte[] imageData = targetUser.getProfileImageData();
        if (imageData == null || imageData.length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(parseMediaType(targetUser.getProfileImageContentType()))
                .body(imageData);
    }

    private MediaType parseMediaType(String mediaTypeValue) {
        if (mediaTypeValue == null || mediaTypeValue.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(mediaTypeValue);
        } catch (InvalidMediaTypeException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
