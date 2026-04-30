package com.example.schedulemanager.dto;

public class ScheduleRequest {
    private String scheduleDate;
    private String title;
    private String startTime;
    private String endTime;
    private String description;
    private Boolean sharedWithFriends;

    public String getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getSharedWithFriends() {
        return sharedWithFriends;
    }

    public void setSharedWithFriends(Boolean sharedWithFriends) {
        this.sharedWithFriends = sharedWithFriends;
    }
}
