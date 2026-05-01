package com.example.schedulemanager.dto;

public class ProfileUpdateRequest {
    private String displayName;
    private String email;
    private String profileBio;
    private String profileIconColor;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileBio() {
        return profileBio;
    }

    public void setProfileBio(String profileBio) {
        this.profileBio = profileBio;
    }

    public String getProfileIconColor() {
        return profileIconColor;
    }

    public void setProfileIconColor(String profileIconColor) {
        this.profileIconColor = profileIconColor;
    }
}
