package com.example.schedulemanager.service;

import com.example.schedulemanager.dto.ScheduleRequest;
import com.example.schedulemanager.mapper.ScheduleMapper;
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

    public ScheduleService(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    @Transactional(readOnly = true)
    public List<ScheduleItem> getByDate(LocalDate date) {
        return scheduleMapper.findByDate(date);
    }

    @Transactional
    public ScheduleItem create(ScheduleRequest request) {
        ScheduleItem item = fromRequest(request);
        scheduleMapper.insert(item);
        return scheduleMapper.findById(item.getId());
    }

    @Transactional
    public ScheduleItem update(Long id, ScheduleRequest request) {
        ScheduleItem existing = scheduleMapper.findById(id);
        if (existing == null) {
            throw new NoSuchElementException("指定された予定が存在しません。id=" + id);
        }

        ScheduleItem item = fromRequest(request);
        item.setId(id);
        scheduleMapper.update(item);
        return scheduleMapper.findById(id);
    }

    @Transactional
    public void delete(Long id) {
        int deletedCount = scheduleMapper.delete(id);
        if (deletedCount == 0) {
            throw new NoSuchElementException("指定された予定が存在しません。id=" + id);
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

        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("終了時刻は開始時刻以降にしてください。");
        }

        ScheduleItem item = new ScheduleItem();
        item.setScheduleDate(scheduleDate);
        item.setTitle(title);
        item.setStartTime(startTime);
        item.setEndTime(endTime);
        item.setDescription(normalize(request.getDescription()));
        return item;
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
}
