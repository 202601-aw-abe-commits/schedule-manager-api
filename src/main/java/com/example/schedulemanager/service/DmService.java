package com.example.schedulemanager.service;

import com.example.schedulemanager.dto.DmMessageSendRequest;
import com.example.schedulemanager.dto.DmStartRequest;
import com.example.schedulemanager.mapper.DirectMessageMapper;
import com.example.schedulemanager.mapper.DmConversationMapper;
import com.example.schedulemanager.mapper.UserMapper;
import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.model.DirectMessage;
import com.example.schedulemanager.model.DmConversation;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DmService {
    private static final long MAX_ATTACHMENT_BYTES = 25L * 1024L * 1024L;
    private static final Set<String> ALLOWED_ATTACHMENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "video/mp4", "video/webm", "video/quicktime");
    private final DmConversationMapper dmConversationMapper;
    private final DirectMessageMapper directMessageMapper;
    private final UserMapper userMapper;
    private final FriendshipService friendshipService;

    public DmService(
            DmConversationMapper dmConversationMapper,
            DirectMessageMapper directMessageMapper,
            UserMapper userMapper,
            FriendshipService friendshipService) {
        this.dmConversationMapper = dmConversationMapper;
        this.directMessageMapper = directMessageMapper;
        this.userMapper = userMapper;
        this.friendshipService = friendshipService;
    }

    @Transactional
    public DmConversation startOrGetConversation(Long currentUserId, DmStartRequest request) {
        if (request == null || request.getPartnerUserId() == null) {
            throw new IllegalArgumentException("相手ユーザーIDを指定してください。");
        }
        Long partnerUserId = request.getPartnerUserId();
        if (currentUserId.equals(partnerUserId)) {
            throw new IllegalArgumentException("自分自身との会話は作成できません。");
        }
        AppUser partner = userMapper.findById(partnerUserId);
        if (partner == null) {
            throw new NoSuchElementException("相手ユーザーが見つかりません。");
        }
        if (!friendshipService.areFriendsOrSelf(currentUserId, partnerUserId)) {
            throw new IllegalArgumentException("フレンド同士のみDMを開始できます。");
        }

        DmConversation existing = dmConversationMapper.findPair(currentUserId, partnerUserId);
        if (existing != null) {
            return existing;
        }

        DmConversation conversation = new DmConversation();
        long userA = Math.min(currentUserId, partnerUserId);
        long userB = Math.max(currentUserId, partnerUserId);
        conversation.setUserAId(userA);
        conversation.setUserBId(userB);
        dmConversationMapper.insert(conversation);
        return dmConversationMapper.findById(conversation.getId());
    }

    @Transactional(readOnly = true)
    public List<DmConversation> listConversations(Long currentUserId) {
        return dmConversationMapper.findByUser(currentUserId);
    }

    @Transactional
    public List<DirectMessage> listMessages(Long currentUserId, Long conversationId) {
        DmConversation conversation = requireAccessibleConversation(currentUserId, conversationId);
        directMessageMapper.markConversationAsRead(currentUserId, conversation.getId());
        return directMessageMapper.findByConversationId(conversation.getId());
    }

    @Transactional
    public DirectMessage sendMessage(Long currentUserId, Long conversationId, DmMessageSendRequest request) {
        return sendMessageInternal(currentUserId, conversationId, request == null ? null : request.getBody(), null);
    }

    @Transactional
    public DirectMessage sendMessageWithAttachment(Long currentUserId, Long conversationId, String body, MultipartFile file) {
        return sendMessageInternal(currentUserId, conversationId, body, file);
    }

    @Transactional(readOnly = true)
    public DirectMessage getMessageAttachment(Long currentUserId, Long messageId) {
        DirectMessage message = directMessageMapper.findById(messageId);
        if (message == null) {
            throw new NoSuchElementException("メッセージが見つかりません。");
        }
        boolean member = currentUserId.equals(message.getSenderUserId()) || currentUserId.equals(message.getRecipientUserId());
        if (!member) {
            throw new IllegalArgumentException("この添付ファイルにはアクセスできません。");
        }
        return message;
    }

    private DirectMessage sendMessageInternal(Long currentUserId, Long conversationId, String rawBody, MultipartFile file) {
        DmConversation conversation = requireAccessibleConversation(currentUserId, conversationId);
        String body = normalize(rawBody);
        boolean hasFile = file != null && !file.isEmpty();
        if (body == null && !hasFile) {
            throw new IllegalArgumentException("メッセージ本文または添付ファイルを入力してください。");
        }
        if (body != null && body.length() > 1000) {
            throw new IllegalArgumentException("メッセージ本文は1000文字以内で入力してください。");
        }

        Long recipientUserId = conversation.getUserAId().equals(currentUserId)
                ? conversation.getUserBId()
                : conversation.getUserAId();

        DirectMessage message = new DirectMessage();
        message.setConversationId(conversationId);
        message.setSenderUserId(currentUserId);
        message.setRecipientUserId(recipientUserId);
        message.setBody(body == null ? "" : body);
        if (hasFile) {
            applyAttachment(message, file);
        }
        message.setRead(false);
        directMessageMapper.insert(message);
        return directMessageMapper.findByConversationId(conversationId).stream()
                .reduce((a, b) -> b)
                .orElseThrow();
    }

    private void applyAttachment(DirectMessage message, MultipartFile file) {
        if (file.getSize() > MAX_ATTACHMENT_BYTES) {
            throw new IllegalArgumentException("添付ファイルは25MB以内にしてください。");
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_ATTACHMENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("添付可能なのは画像(JPEG/PNG/WEBP/GIF)または動画(MP4/WEBM/MOV)です。");
        }
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException ex) {
            throw new IllegalArgumentException("添付ファイルの読み込みに失敗しました。");
        }
        if (data.length == 0) {
            throw new IllegalArgumentException("空の添付ファイルは送信できません。");
        }
        message.setAttachmentData(data);
        message.setAttachmentContentType(contentType);
        message.setAttachmentFileName(normalizeFileName(file.getOriginalFilename()));
        message.setAttachmentSize((long) data.length);
        message.setHasAttachment(true);
    }

    private DmConversation requireAccessibleConversation(Long currentUserId, Long conversationId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("会話IDを指定してください。");
        }
        DmConversation conversation = dmConversationMapper.findById(conversationId);
        if (conversation == null) {
            throw new NoSuchElementException("会話が見つかりません。");
        }
        boolean member = currentUserId.equals(conversation.getUserAId()) || currentUserId.equals(conversation.getUserBId());
        if (!member) {
            throw new IllegalArgumentException("この会話にはアクセスできません。");
        }
        return conversation;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizeContentType(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private String normalizeFileName(String value) {
        if (value == null) {
            return "attachment";
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return "attachment";
        }
        return trimmed.length() > 300 ? trimmed.substring(0, 300) : trimmed;
    }
}
