package com.example.schedulemanager.service;

import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.model.ScheduleItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(MailNotificationService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:}")
    private String fromAddress;

    public MailNotificationService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async("mailTaskExecutor")
    public void sendTextMail(String to, String subject, String textBody) {
        if (to == null || to.isBlank()) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(textBody);
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("テキストメール送信に失敗しました。to={}", to, ex);
        }
    }

    @Async("mailTaskExecutor")
    public void sendDueReminderMail(AppUser user, LocalDate dueDate, List<ScheduleItem> dueItems) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        if (dueItems == null || dueItems.isEmpty()) {
            return;
        }

        String subject = String.format(Locale.JAPAN, "【リマインダー】%s の予定", dueDate);
        String plainText = buildDueReminderPlainText(user, dueDate, dueItems);
        Context context = new Context(Locale.JAPAN);
        context.setVariable("displayName", user.getDisplayName());
        context.setVariable("dueDate", dueDate.toString());
        context.setVariable("items", dueItems);
        String htmlBody = templateEngine.process("mail/due-reminder", context);

        sendHtmlMultipartMail(user.getEmail(), subject, plainText, htmlBody, Map.of());
    }

    private void sendHtmlMultipartMail(
            String to,
            String subject,
            String plainTextBody,
            String htmlBody,
            Map<String, byte[]> attachments) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainTextBody, htmlBody);
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }

            for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
                helper.addAttachment(entry.getKey(), new ByteArrayResource(entry.getValue()));
            }

            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            log.warn("HTMLメール作成に失敗しました。to={}", to, ex);
        } catch (Exception ex) {
            log.warn("HTMLメール送信に失敗しました。to={}", to, ex);
        }
    }

    private String buildDueReminderPlainText(AppUser user, LocalDate dueDate, List<ScheduleItem> dueItems) {
        StringBuilder builder = new StringBuilder();
        builder.append(user.getDisplayName()).append(" さん").append(System.lineSeparator());
        builder.append(dueDate).append(" の予定リマインダーです。").append(System.lineSeparator()).append(System.lineSeparator());

        for (ScheduleItem item : dueItems) {
            builder.append("- ").append(item.getTitle());
            if (item.getStartTime() != null) {
                builder.append(" (").append(item.getStartTime()).append(")");
            }
            builder.append(System.lineSeparator());
        }
        builder.append(System.lineSeparator()).append("Schedule Manager");
        return builder.toString();
    }
}
