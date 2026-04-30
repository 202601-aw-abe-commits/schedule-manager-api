package com.example.schedulemanager.service;

import com.example.schedulemanager.dto.ScheduleRequest;
import com.example.schedulemanager.mapper.ScheduleMapper;
import com.example.schedulemanager.mapper.UserMapper;
import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.model.ScheduleItem;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {
    private final ScheduleMapper scheduleMapper;
    private final UserMapper userMapper;

    public ScheduleService(ScheduleMapper scheduleMapper, UserMapper userMapper) {
        this.scheduleMapper = scheduleMapper;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<ScheduleItem> getByDate(LocalDate date, String currentUsername) {
        AppUser currentUser = getCurrentUser(currentUsername);
        List<ScheduleItem> items = scheduleMapper.findVisibleByDate(date, currentUser.getId());
        decorateForViewer(items, currentUser.getId());
        return items;
    }

    @Transactional
    public ScheduleItem create(ScheduleRequest request, String currentUsername) {
        AppUser currentUser = getCurrentUser(currentUsername);
        ScheduleItem item = fromRequest(request);
        item.setOwnerUserId(currentUser.getId());
        scheduleMapper.insert(item);
        ScheduleItem created = scheduleMapper.findVisibleById(item.getId(), currentUser.getId());
        decorateForViewer(created, currentUser.getId());
        return created;
    }

    @Transactional
    public ScheduleItem update(Long id, ScheduleRequest request, String currentUsername) {
        AppUser currentUser = getCurrentUser(currentUsername);
        ScheduleItem existing = scheduleMapper.findOwnedById(id, currentUser.getId());
        if (existing == null) {
            throw new NoSuchElementException("指定された予定が存在しないか、編集権限がありません。id=" + id);
        }

        ScheduleItem item = fromRequest(request);
        item.setId(id);
        item.setOwnerUserId(currentUser.getId());
        int updatedCount = scheduleMapper.update(item);
        if (updatedCount == 0) {
            throw new NoSuchElementException("指定された予定が存在しないか、編集権限がありません。id=" + id);
        }
        if (!Boolean.TRUE.equals(item.getJoinable())) {
            scheduleMapper.deleteAllParticipantsBySchedule(id);
        }

        ScheduleItem updated = scheduleMapper.findVisibleById(id, currentUser.getId());
        decorateForViewer(updated, currentUser.getId());
        return updated;
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        AppUser currentUser = getCurrentUser(currentUsername);
        ScheduleItem existing = scheduleMapper.findOwnedById(id, currentUser.getId());
        if (existing == null) {
            throw new NoSuchElementException("指定された予定が存在しないか、削除権限がありません。id=" + id);
        }

        scheduleMapper.deleteAllParticipantsBySchedule(id);
        int deletedCount = scheduleMapper.delete(id, currentUser.getId());
        if (deletedCount == 0) {
            throw new NoSuchElementException("指定された予定が存在しないか、削除権限がありません。id=" + id);
        }
    }

    @Transactional
    public void join(Long id, String currentUsername) {
        AppUser currentUser = getCurrentUser(currentUsername);
        ScheduleItem target = scheduleMapper.findVisibleById(id, currentUser.getId());
        validateJoinable(target, currentUser);

        if (scheduleMapper.existsParticipant(id, currentUser.getId())) {
            throw new IllegalArgumentException("すでに参加しています。");
        }

        scheduleMapper.insertParticipant(id, currentUser.getId());
    }

    @Transactional
    public void cancelJoin(Long id, String currentUsername) {
        AppUser currentUser = getCurrentUser(currentUsername);
        ScheduleItem target = scheduleMapper.findVisibleById(id, currentUser.getId());
        validateJoinable(target, currentUser);

        int deletedCount = scheduleMapper.deleteParticipant(id, currentUser.getId());
        if (deletedCount == 0) {
            throw new NoSuchElementException("参加情報が見つかりません。");
        }
    }

    private ScheduleItem fromRequest(ScheduleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("リクエストが空です。");
        }

        String title = normalize(request.getTitle());
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("タイトルは必須です。");
        }

        LocalDate scheduleDate;
        try {
            scheduleDate = LocalDate.parse(normalize(request.getScheduleDate()));
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("日付は YYYY-MM-DD 形式で指定してください。");
        }

        LocalTime startTime = parseTime(request.getStartTime(), "開始時刻");
        LocalTime endTime = parseTime(request.getEndTime(), "終了時刻");
        boolean joinable = Boolean.TRUE.equals(request.getJoinable());

        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("終了時刻は開始時刻以降にしてください。");
        }
        if (joinable && startTime == null) {
            throw new IllegalArgumentException("参加募集予定は開始時刻を入力してください。");
        }

        ScheduleItem item = new ScheduleItem();
        item.setScheduleDate(scheduleDate);
        item.setTitle(title);
        item.setStartTime(startTime);
        item.setEndTime(endTime);
        item.setDescription(normalize(request.getDescription()));
        boolean sharedWithFriends = Boolean.TRUE.equals(request.getSharedWithFriends()) || joinable;
        item.setSharedWithFriends(sharedWithFriends);
        item.setJoinable(joinable);
        return item;
    }

    private AppUser getCurrentUser(String username) {
        AppUser user = userMapper.findByUsername(username);
        if (user == null) {
            throw new NoSuchElementException("ログインユーザーが見つかりません。");
        }
        return user;
    }

    private LocalTime parseTime(String value, String fieldName) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }

        try {
            return LocalTime.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + "は HH:mm 形式で指定してください。");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private void decorateForViewer(ScheduleItem item, Long viewerUserId) {
        if (item == null) {
            return;
        }
        item.setEditable(viewerUserId.equals(item.getOwnerUserId()));
        attachParticipationInfo(item, viewerUserId);
    }

    private void decorateForViewer(List<ScheduleItem> items, Long viewerUserId) {
        for (ScheduleItem item : items) {
            decorateForViewer(item, viewerUserId);
        }
    }

    private void attachParticipationInfo(ScheduleItem item, Long viewerUserId) {
        if (!Boolean.TRUE.equals(item.getJoinable())) {
            item.setParticipantCount(0);
            item.setJoinedByCurrentUser(false);
            item.setParticipants(List.of());
            return;
        }
        item.setParticipantCount(scheduleMapper.countParticipants(item.getId()));
        item.setJoinedByCurrentUser(scheduleMapper.existsParticipant(item.getId(), viewerUserId));
        item.setParticipants(scheduleMapper.findParticipants(item.getId()));
    }

    private void validateJoinable(ScheduleItem target, AppUser currentUser) {
        if (target == null) {
            throw new NoSuchElementException("対象の予定が見つかりません。");
        }
        if (!Boolean.TRUE.equals(target.getJoinable())) {
            throw new IllegalArgumentException("この予定は参加募集していません。");
        }
        if (currentUser.getId().equals(target.getOwnerUserId())) {
            throw new IllegalArgumentException("作成者は自動的に参加者として扱われます。");
        }
    }
}
