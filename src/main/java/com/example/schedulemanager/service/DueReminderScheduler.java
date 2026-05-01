package com.example.schedulemanager.service;

import com.example.schedulemanager.mapper.ScheduleMapper;
import com.example.schedulemanager.mapper.UserMapper;
import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.model.ScheduleItem;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DueReminderScheduler {
    private static final Logger log = LoggerFactory.getLogger(DueReminderScheduler.class);

    private final UserMapper userMapper;
    private final ScheduleMapper scheduleMapper;
    private final MailNotificationService mailNotificationService;

    @Value("${app.reminder.enabled:true}")
    private boolean reminderEnabled;

    @Value("${app.reminder.zone:Asia/Tokyo}")
    private String reminderZone;

    public DueReminderScheduler(
            UserMapper userMapper,
            ScheduleMapper scheduleMapper,
            MailNotificationService mailNotificationService) {
        this.userMapper = userMapper;
        this.scheduleMapper = scheduleMapper;
        this.mailNotificationService = mailNotificationService;
    }

    @Scheduled(cron = "${app.reminder.cron:0 0 9 * * *}", zone = "${app.reminder.zone:Asia/Tokyo}")
    public void sendDailyDueReminders() {
        if (!reminderEnabled) {
            return;
        }

        LocalDate dueDate = LocalDate.now(ZoneId.of(reminderZone));
        List<AppUser> users = userMapper.findEnabledUsersWithEmail();
        for (AppUser user : users) {
            List<ScheduleItem> dueItems = scheduleMapper.findOwnedByDate(dueDate, user.getId());
            if (dueItems.isEmpty()) {
                continue;
            }
            mailNotificationService.sendDueReminderMail(user, dueDate, dueItems);
        }
        log.info("期限リマインダー送信を実行しました。date={}, targetUsers={}", dueDate, users.size());
    }
}
