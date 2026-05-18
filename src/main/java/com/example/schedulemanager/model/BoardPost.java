package com.example.schedulemanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class BoardPost {
    private Long id;
    private Long threadId;
    private String gameTitle;
    private Long authorUserId;
    private String authorUsername;
    private String authorDisplayName;
    private String body;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private String rankBand;
    private Integer recruitmentLimit;
    private String discordInviteUrl;
    private Integer participantCount;
    private Integer remainingRecruitmentSlots;
    private Boolean recruitmentClosed;
    private Boolean joinedByCurrentUser;
    private String joinRequestStatusForCurrentUser;
    private List<BoardJoinRequest> pendingJoinRequests;
    private List<FriendUser> participants;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public Long getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(Long authorUserId) {
        this.authorUserId = authorUserId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public void setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public Integer getRecruitmentLimit() {
        return recruitmentLimit;
    }

    public void setRecruitmentLimit(Integer recruitmentLimit) {
        this.recruitmentLimit = recruitmentLimit;
    }

    public String getDiscordInviteUrl() {
        return discordInviteUrl;
    }

    public void setDiscordInviteUrl(String discordInviteUrl) {
        this.discordInviteUrl = discordInviteUrl;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }

    public Integer getRemainingRecruitmentSlots() {
        return remainingRecruitmentSlots;
    }

    public void setRemainingRecruitmentSlots(Integer remainingRecruitmentSlots) {
        this.remainingRecruitmentSlots = remainingRecruitmentSlots;
    }

    public Boolean getRecruitmentClosed() {
        return recruitmentClosed;
    }

    public void setRecruitmentClosed(Boolean recruitmentClosed) {
        this.recruitmentClosed = recruitmentClosed;
    }

    public Boolean getJoinedByCurrentUser() {
        return joinedByCurrentUser;
    }

    public void setJoinedByCurrentUser(Boolean joinedByCurrentUser) {
        this.joinedByCurrentUser = joinedByCurrentUser;
    }

    public String getJoinRequestStatusForCurrentUser() {
        return joinRequestStatusForCurrentUser;
    }

    public void setJoinRequestStatusForCurrentUser(String joinRequestStatusForCurrentUser) {
        this.joinRequestStatusForCurrentUser = joinRequestStatusForCurrentUser;
    }

    public List<BoardJoinRequest> getPendingJoinRequests() {
        return pendingJoinRequests;
    }

    public void setPendingJoinRequests(List<BoardJoinRequest> pendingJoinRequests) {
        this.pendingJoinRequests = pendingJoinRequests;
    }

    public List<FriendUser> getParticipants() {
        return participants;
    }

    public void setParticipants(List<FriendUser> participants) {
        this.participants = participants;
    }

    public String getRankBand() {
        return rankBand;
    }

    public void setRankBand(String rankBand) {
        this.rankBand = rankBand;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
