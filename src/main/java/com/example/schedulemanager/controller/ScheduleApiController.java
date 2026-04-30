package com.example.schedulemanager.controller;

import com.example.schedulemanager.dto.ScheduleRequest;
import com.example.schedulemanager.model.ScheduleItem;
import com.example.schedulemanager.service.ScheduleService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleApiController {
    private final ScheduleService scheduleService;

    public ScheduleApiController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public List<ScheduleItem> findByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        return scheduleService.getByDate(date, userDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<ScheduleItem> create(
            @RequestBody ScheduleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.create(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ScheduleItem update(
            @PathVariable("id") Long id,
            @RequestBody ScheduleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return scheduleService.update(id, request, userDetails.getUsername());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        scheduleService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
