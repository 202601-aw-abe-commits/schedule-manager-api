package com.example.schedulemanager.controller;

import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.service.FriendshipService;
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
    private final FriendshipService friendshipService;

    public UserLookupApiController(
            UserAccountService userAccountService,
            FriendshipService friendshipService) {
        this.userAccountService = userAccountService;
        this.friendshipService = friendshipService;
    }

    @GetMapping("/{userId}/profile-image")
    public ResponseEntity<byte[]> profileImage(
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = userAccountService.getByUsername(userDetails.getUsername());
        boolean canView = friendshipService.areFriendsOrSelf(currentUser.getId(), userId);
        if (!canView) {
            return ResponseEntity.notFound().build();
        }

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
