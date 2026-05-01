package com.example.schedulemanager.dto;

public class DirectMessageSendRequest {
    private String recipientUsername;
    private String body;
    private Long relatedScheduleItemId;

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getRelatedScheduleItemId() {
        return relatedScheduleItemId;
    }

    public void setRelatedScheduleItemId(Long relatedScheduleItemId) {
        this.relatedScheduleItemId = relatedScheduleItemId;
    }
}
