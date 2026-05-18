package com.example.schedulemanager.model;

import java.time.LocalDateTime;

public class FriendRequestInfo {
    private Long id;
    private Long requesterUserId;
    private String requesterUsername;
    private String requesterDisplayName;
    private String profileIconColor;
    private Boolean hasProfileImage;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(Long requesterUserId) {
        this.requesterUserId = requesterUserId;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public String getRequesterDisplayName() {
        return requesterDisplayName;
    }

    public void setRequesterDisplayName(String requesterDisplayName) {
        this.requesterDisplayName = requesterDisplayName;
    }

    public String getProfileIconColor() {
        return profileIconColor;
    }

    public void setProfileIconColor(String profileIconColor) {
        this.profileIconColor = profileIconColor;
    }

    public Boolean getHasProfileImage() {
        return hasProfileImage;
    }

    public void setHasProfileImage(Boolean hasProfileImage) {
        this.hasProfileImage = hasProfileImage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
