package com.example.schedulemanager.controller;

import com.example.schedulemanager.dto.DmMessageSendRequest;
import com.example.schedulemanager.dto.DmStartRequest;
import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.model.DirectMessage;
import com.example.schedulemanager.model.DmConversation;
import com.example.schedulemanager.service.DmService;
import com.example.schedulemanager.service.UserAccountService;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/dm")
public class DmApiController {
    private final DmService dmService;
    private final UserAccountService userAccountService;

    public DmApiController(DmService dmService, UserAccountService userAccountService) {
        this.dmService = dmService;
        this.userAccountService = userAccountService;
    }

    @PostMapping("/start")
    public DmConversation start(
            @RequestBody DmStartRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userAccountService.getByUsername(userDetails.getUsername());
        return dmService.startOrGetConversation(user.getId(), request);
    }

    @GetMapping("/conversations")
    public List<DmConversation> conversations(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userAccountService.getByUsername(userDetails.getUsername());
        return dmService.listConversations(user.getId());
    }

    @GetMapping("/conversations/{id}/messages")
    public List<DirectMessage> messages(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userAccountService.getByUsername(userDetails.getUsername());
        return dmService.listMessages(user.getId(), id);
    }

    @PostMapping("/conversations/{id}/messages")
    public DirectMessage sendMessage(
            @PathVariable("id") Long id,
            @RequestBody DmMessageSendRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userAccountService.getByUsername(userDetails.getUsername());
        return dmService.sendMessage(user.getId(), id, request);
    }

    @PostMapping("/conversations/{id}/messages/upload")
    public DirectMessage sendMessageWithAttachment(
            @PathVariable("id") Long id,
            @RequestParam(value = "body", required = false) String body,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userAccountService.getByUsername(userDetails.getUsername());
        return dmService.sendMessageWithAttachment(user.getId(), id, body, file);
    }

    @GetMapping("/messages/{messageId}/attachment")
    public ResponseEntity<byte[]> attachment(
            @PathVariable("messageId") Long messageId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userAccountService.getByUsername(userDetails.getUsername());
        DirectMessage message = dmService.getMessageAttachment(user.getId(), messageId);
        if (message.getAttachmentData() == null || message.getAttachmentData().length == 0) {
            return ResponseEntity.notFound().build();
        }
        String contentType = message.getAttachmentContentType();
        MediaType mediaType;
        try {
            mediaType = contentType == null || contentType.isBlank()
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        String fileName = message.getAttachmentFileName() == null || message.getAttachmentFileName().isBlank()
                ? "attachment"
                : message.getAttachmentFileName();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName.replace("\"", "") + "\"")
                .body(message.getAttachmentData());
    }
}
