package com.example.schedulemanager.dto;

public class ScheduleJoinRequestCreateRequest {
    private String comment;
    private String gameId;

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
}
