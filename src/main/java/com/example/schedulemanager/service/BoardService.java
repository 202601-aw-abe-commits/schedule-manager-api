package com.example.schedulemanager.service;

import com.example.schedulemanager.dto.BoardRecruitmentCreateRequest;
import com.example.schedulemanager.dto.BoardRecruitmentUpdateRequest;
import com.example.schedulemanager.dto.BoardJoinRequestCreateRequest;
import com.example.schedulemanager.dto.BoardPostInterestCreateRequest;
import com.example.schedulemanager.dto.BoardThreadCreateRequest;
import com.example.schedulemanager.dto.BoardDiscordInviteRequest;
import com.example.schedulemanager.mapper.BoardMapper;
import com.example.schedulemanager.mapper.UserMapper;
import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.model.BoardJoinRequest;
import com.example.schedulemanager.model.BoardPost;
import com.example.schedulemanager.model.BoardPostInterest;
import com.example.schedulemanager.model.BoardThread;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {
    private final BoardMapper boardMapper;
    private final UserMapper userMapper;
    private final NotificationEventService notificationEventService;
    private final FriendNotificationPreferenceService friendNotificationPreferenceService;

    public BoardService(
            BoardMapper boardMapper,
            UserMapper userMapper,
            NotificationEventService notificationEventService,
            FriendNotificationPreferenceService friendNotificationPreferenceService) {
        this.boardMapper = boardMapper;
        this.userMapper = userMapper;
        this.notificationEventService = notificationEventService;
        this.friendNotificationPreferenceService = friendNotificationPreferenceService;
    }

    @Transactional(readOnly = true)
    public List<BoardThread> listThreads() {
        return listThreads(null);
    }

    @Transactional(readOnly = true)
    public List<BoardThread> listThreads(String keyword) {
        return boardMapper.findAllThreads(normalize(keyword));
    }

    @Transactional(readOnly = true)
    public List<BoardPost> listPosts(Long threadId) {
        ensureThreadExists(threadId);
        return boardMapper.findPostsByThreadId(threadId);
    }

    @Transactional(readOnly = true)
    public List<BoardPost> listPosts(Long threadId, String username) {
        ensureThreadExists(threadId);
        AppUser viewer = findCurrentUser(username);
        List<BoardPost> posts = boardMapper.findPostsByThreadId(threadId);
        decorateForViewer(posts, viewer.getId());
        return posts;
    }

    @Transactional
    public BoardThread createThread(BoardThreadCreateRequest request, String username) {
        String gameTitle = normalize(request == null ? null : request.getGameTitle());
        if (gameTitle == null || gameTitle.isBlank()) {
            throw new IllegalArgumentException("ゲームタイトルを入力してください。");
        }
        if (gameTitle.length() > 200) {
            throw new IllegalArgumentException("ゲームタイトルは200文字以内で入力してください。");
        }

        AppUser user = findCurrentUser(username);
        BoardThread thread = new BoardThread();
        thread.setOwnerUserId(user.getId());
        thread.setGameTitle(gameTitle);
        boardMapper.insertThread(thread);
        BoardThread created = boardMapper.findThreadViewById(thread.getId());
        if (created == null) {
            throw new NoSuchElementException("作成したスレッドの取得に失敗しました。");
        }
        return created;
    }

    @Transactional
    public BoardPost createRecruitment(Long threadId, BoardRecruitmentCreateRequest request, String username) {
        ensureThreadExists(threadId);
        if (request == null) {
            throw new IllegalArgumentException("募集内容が空です。");
        }

        String body = normalize(request.getBody());
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("募集内容を入力してください。");
        }
        if (body.length() > 1000) {
            throw new IllegalArgumentException("募集内容は1000文字以内で入力してください。");
        }

        LocalDate scheduleDate = parseDate(request.getScheduleDate());
        LocalTime startTime = parseTime(request.getStartTime());
        String rankBand = normalize(request.getRankBand());
        if (rankBand != null && rankBand.length() > 100) {
            throw new IllegalArgumentException("ランクは100文字以内で入力してください。");
        }
        Integer recruitmentLimit = request.getRecruitmentLimit();
        if (recruitmentLimit != null && recruitmentLimit < 1) {
            throw new IllegalArgumentException("募集人数は1以上で指定してください。");
        }
        String discordInviteUrl = normalizeDiscordInviteUrl(request.getDiscordInviteUrl());

        AppUser user = findCurrentUser(username);
        BoardPost post = new BoardPost();
        post.setThreadId(threadId);
        post.setAuthorUserId(user.getId());
        post.setBody(body);
        post.setScheduleDate(scheduleDate);
        post.setStartTime(startTime);
        post.setRankBand(rankBand);
        post.setRecruitmentLimit(recruitmentLimit);
        post.setDiscordInviteUrl(discordInviteUrl);
        boardMapper.insertPost(post);
        boardMapper.touchThreadUpdatedAt(threadId);
        notifyFriendFollowersForRecruitment(user, threadId);

        return boardMapper.findPostsByThreadId(threadId).stream()
                .filter(row -> row.getId().equals(post.getId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("作成した募集投稿の取得に失敗しました。"));
    }

    @Transactional(readOnly = true)
    public List<BoardPostInterest> listInterests(Long postId) {
        ensurePostExists(postId);
        return boardMapper.findInterestsByPostId(postId);
    }

    @Transactional
    public BoardPostInterest createInterest(Long postId, BoardPostInterestCreateRequest request, String username) {
        BoardPost post = ensurePostExists(postId);
        if (request == null) {
            throw new IllegalArgumentException("参加希望コメントが空です。");
        }
        String comment = normalize(request.getComment());
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("参加希望コメントを入力してください。");
        }
        if (comment.length() > 500) {
            throw new IllegalArgumentException("参加希望コメントは500文字以内で入力してください。");
        }

        AppUser user = findCurrentUser(username);
        if (post.getAuthorUserId() != null && post.getAuthorUserId().equals(user.getId())) {
            throw new IllegalArgumentException("自分の募集には参加希望を送信できません。");
        }

        BoardPostInterest interest = new BoardPostInterest();
        interest.setPostId(postId);
        interest.setRequesterUserId(user.getId());
        interest.setComment(comment);
        boardMapper.insertPostInterest(interest);
        if (post.getAuthorUserId() != null && !post.getAuthorUserId().equals(user.getId())) {
            BoardThread thread = post.getThreadId() == null ? null : boardMapper.findThreadById(post.getThreadId());
            String threadTitle = thread == null || thread.getGameTitle() == null ? "募集投稿" : thread.getGameTitle();
            String actorName = user.getDisplayName() == null ? user.getUsername() : user.getDisplayName();
            notificationEventService.publish(
                    post.getAuthorUserId(),
                    user.getId(),
                    "BOARD_INTEREST",
                    "参加希望コメント",
                    actorName + " さんが「" + threadTitle + "」に参加希望コメントを投稿しました。");
        }

        return boardMapper.findInterestsByPostId(postId).stream()
                .filter(row -> row.getId().equals(interest.getId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("作成した参加希望の取得に失敗しました。"));
    }

    @Transactional
    public BoardPost updateRecruitment(Long postId, BoardRecruitmentUpdateRequest request, String username) {
        BoardPost existing = ensurePostExists(postId);
        if (request == null) {
            throw new IllegalArgumentException("更新内容が空です。");
        }

        String body = normalize(request.getBody());
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("募集内容を入力してください。");
        }
        if (body.length() > 1000) {
            throw new IllegalArgumentException("募集内容は1000文字以内で入力してください。");
        }

        LocalDate scheduleDate = parseDate(request.getScheduleDate());
        LocalTime startTime = parseTime(request.getStartTime());
        String rankBand = normalize(request.getRankBand());
        if (rankBand != null && rankBand.length() > 100) {
            throw new IllegalArgumentException("ランクは100文字以内で入力してください。");
        }
        Integer recruitmentLimit = request.getRecruitmentLimit();
        if (recruitmentLimit != null && recruitmentLimit < 1) {
            throw new IllegalArgumentException("募集人数は1以上で指定してください。");
        }

        AppUser currentUser = findCurrentUser(username);
        int updated = boardMapper.updatePostByAuthor(
                postId,
                currentUser.getId(),
                body,
                scheduleDate,
                startTime,
                rankBand,
                recruitmentLimit);
        if (updated == 0) {
            throw new NoSuchElementException("指定された投稿が存在しないか、更新権限がありません。");
        }

        if (existing.getThreadId() != null) {
            boardMapper.touchThreadUpdatedAt(existing.getThreadId());
        }

        BoardPost updatedPost = boardMapper.findPostById(postId);
        if (updatedPost == null) {
            throw new NoSuchElementException("更新した投稿の取得に失敗しました。");
        }
        return updatedPost;
    }

    @Transactional
    public void deleteRecruitment(Long postId, String username) {
        BoardPost existing = ensurePostExists(postId);
        AppUser currentUser = findCurrentUser(username);
        int deleted = boardMapper.deletePostByAuthor(postId, currentUser.getId());
        if (deleted == 0) {
            throw new NoSuchElementException("指定された投稿が存在しないか、削除権限がありません。");
        }
        if (existing.getThreadId() != null) {
            boardMapper.touchThreadUpdatedAt(existing.getThreadId());
        }
    }

    @Transactional
    public void joinPost(Long postId, String username, BoardJoinRequestCreateRequest request) {
        BoardPost post = ensurePostExists(postId);
        AppUser currentUser = findCurrentUser(username);
        validateJoinablePost(post, currentUser);

        if (boardMapper.existsParticipant(postId, currentUser.getId())) {
            throw new IllegalArgumentException("すでに参加しています。");
        }
        if (isRecruitmentClosed(post, boardMapper.countParticipants(postId))) {
            throw new IllegalArgumentException("この募集は締め切られています。");
        }

        String comment = normalizeJoinRequestComment(request == null ? null : request.getComment());
        BoardJoinRequest existing = boardMapper.findJoinRequestByPostAndRequester(postId, currentUser.getId());
        if (existing == null) {
            boardMapper.insertJoinRequest(postId, currentUser.getId(), comment, "PENDING");
        } else {
            boardMapper.updateJoinRequest(existing.getId(), comment, "PENDING");
        }

        if (post.getAuthorUserId() != null && !post.getAuthorUserId().equals(currentUser.getId())) {
            String actorName = currentUser.getDisplayName() == null ? currentUser.getUsername() : currentUser.getDisplayName();
            String threadTitle = post.getGameTitle() == null ? "募集投稿" : post.getGameTitle();
            notificationEventService.publish(
                    post.getAuthorUserId(),
                    currentUser.getId(),
                    "BOARD_JOIN_REQUEST",
                    "参加希望通知",
                    actorName + " さんが「" + threadTitle + "」に参加希望を送信しました。");
        }
    }

    @Transactional
    public void decidePostJoinRequest(Long postId, Long joinRequestId, boolean approve, String username) {
        BoardPost owned = ensurePostExists(postId);
        AppUser owner = findCurrentUser(username);
        if (owned.getAuthorUserId() == null || !owned.getAuthorUserId().equals(owner.getId())) {
            throw new NoSuchElementException("対象の募集が見つかりません。");
        }
        List<BoardJoinRequest> pending = boardMapper.findPendingJoinRequestsByPost(postId);
        BoardJoinRequest target = pending.stream()
                .filter(row -> row.getId().equals(joinRequestId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("参加希望が見つかりません。"));

        if (approve) {
            if (isRecruitmentClosed(owned, boardMapper.countParticipants(postId))) {
                throw new IllegalArgumentException("この募集は締め切られているため承認できません。");
            }
            if (!boardMapper.existsParticipant(postId, target.getRequesterUserId())) {
                boardMapper.insertParticipant(postId, target.getRequesterUserId());
            }
            boardMapper.updateJoinRequestStatus(joinRequestId, "APPROVED");
            notificationEventService.publish(
                    target.getRequesterUserId(),
                    owner.getId(),
                    "BOARD_JOIN_APPROVED",
                    "参加承認",
                    "募集投稿への参加希望が承認されました。");
            return;
        }

        boardMapper.updateJoinRequestStatus(joinRequestId, "REJECTED");
        notificationEventService.publish(
                target.getRequesterUserId(),
                owner.getId(),
                "BOARD_JOIN_REJECTED",
                "参加見送り",
                "募集投稿への参加希望は見送られました。");
    }

    @Transactional
    public BoardPost updatePostDiscordInvite(Long postId, String username, BoardDiscordInviteRequest request) {
        BoardPost existing = ensurePostExists(postId);
        AppUser owner = findCurrentUser(username);
        String inviteUrl = normalizeDiscordInviteUrl(request == null ? null : request.getDiscordInviteUrl());
        int updated = boardMapper.updateDiscordInviteByAuthor(postId, owner.getId(), inviteUrl);
        if (updated == 0) {
            throw new NoSuchElementException("指定された投稿が存在しないか、更新権限がありません。");
        }
        if (existing.getThreadId() != null) {
            boardMapper.touchThreadUpdatedAt(existing.getThreadId());
        }
        BoardPost post = boardMapper.findPostById(postId);
        if (post == null) {
            throw new NoSuchElementException("更新後の投稿取得に失敗しました。");
        }
        decorateForViewer(post, owner.getId());
        return post;
    }

    private void ensureThreadExists(Long threadId) {
        if (threadId == null) {
            throw new IllegalArgumentException("スレッドIDが不正です。");
        }
        BoardThread existing = boardMapper.findThreadById(threadId);
        if (existing == null) {
            throw new NoSuchElementException("指定されたスレッドが見つかりません。");
        }
    }

    private BoardPost ensurePostExists(Long postId) {
        if (postId == null) {
            throw new IllegalArgumentException("投稿IDが不正です。");
        }
        BoardPost existing = boardMapper.findPostById(postId);
        if (existing == null) {
            throw new NoSuchElementException("指定された投稿が見つかりません。");
        }
        return existing;
    }

    private void decorateForViewer(List<BoardPost> posts, Long viewerUserId) {
        for (BoardPost post : posts) {
            decorateForViewer(post, viewerUserId);
        }
    }

    private void decorateForViewer(BoardPost post, Long viewerUserId) {
        int participantCount = boardMapper.countParticipants(post.getId());
        post.setParticipantCount(participantCount);
        post.setRemainingRecruitmentSlots(calcRemainingRecruitmentSlots(post.getRecruitmentLimit(), participantCount));
        post.setRecruitmentClosed(isRecruitmentClosed(post, participantCount));
        post.setJoinedByCurrentUser(boardMapper.existsParticipant(post.getId(), viewerUserId));
        post.setParticipants(boardMapper.findParticipants(post.getId()));
        BoardJoinRequest own = boardMapper.findJoinRequestByPostAndRequester(post.getId(), viewerUserId);
        post.setJoinRequestStatusForCurrentUser(own == null ? null : own.getStatus());
        if (viewerUserId.equals(post.getAuthorUserId())) {
            post.setPendingJoinRequests(boardMapper.findPendingJoinRequestsByPost(post.getId()));
        } else {
            post.setPendingJoinRequests(List.of());
        }
    }

    private AppUser findCurrentUser(String username) {
        AppUser user = userMapper.findByUsername(username);
        if (user == null) {
            throw new NoSuchElementException("ログインユーザーが見つかりません。");
        }
        return user;
    }

    private LocalDate parseDate(String value) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("日付は YYYY-MM-DD 形式で指定してください。");
        }
    }

    private LocalTime parseTime(String value) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("時刻は HH:mm 形式で指定してください。");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private String normalizeJoinRequestComment(String value) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("参加希望コメントを入力してください。");
        }
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("参加希望コメントは500文字以内で入力してください。");
        }
        return normalized;
    }

    private String normalizeDiscordInviteUrl(String value) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        if (normalized.length() > 1000) {
            throw new IllegalArgumentException("Discord招待URLは1000文字以内で入力してください。");
        }
        String lower = normalized.toLowerCase();
        boolean valid = lower.startsWith("https://discord.gg/")
                || lower.startsWith("https://discord.com/invite/")
                || lower.startsWith("https://www.discord.gg/")
                || lower.startsWith("https://www.discord.com/invite/");
        if (!valid) {
            throw new IllegalArgumentException("Discord招待URLの形式が不正です。");
        }
        return normalized;
    }

    private void validateJoinablePost(BoardPost post, AppUser currentUser) {
        if (post == null) {
            throw new NoSuchElementException("対象の投稿が見つかりません。");
        }
        if (currentUser.getId().equals(post.getAuthorUserId())) {
            throw new IllegalArgumentException("作成者は自分の募集に参加希望できません。");
        }
    }

    private Integer calcRemainingRecruitmentSlots(Integer recruitmentLimit, int participantCount) {
        if (recruitmentLimit == null) {
            return null;
        }
        return Math.max(recruitmentLimit - participantCount, 0);
    }

    private boolean isRecruitmentClosed(BoardPost post, int participantCount) {
        Integer limit = post.getRecruitmentLimit();
        return limit != null && participantCount >= limit;
    }

    private void notifyFriendFollowersForRecruitment(AppUser actor, Long threadId) {
        if (actor == null || actor.getId() == null) {
            return;
        }
        List<Long> recipients = friendNotificationPreferenceService.findRecipientsByActor(actor.getId());
        if (recipients.isEmpty()) {
            return;
        }
        BoardThread thread = boardMapper.findThreadById(threadId);
        String gameTitle = thread == null || thread.getGameTitle() == null ? "募集投稿" : thread.getGameTitle();
        String actorName = actor.getDisplayName() == null ? actor.getUsername() : actor.getDisplayName();
        for (Long recipientUserId : recipients) {
            notificationEventService.publish(
                    recipientUserId,
                    actor.getId(),
                    "FRIEND_BOARD_UPDATE",
                    "フレンド募集通知",
                    actorName + " さんが「" + gameTitle + "」の募集を投稿しました。");
        }
    }
}
