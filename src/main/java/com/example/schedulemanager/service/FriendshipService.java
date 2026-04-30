package com.example.schedulemanager.service;

import com.example.schedulemanager.dto.FriendRequestCreateRequest;
import com.example.schedulemanager.mapper.FriendshipMapper;
import com.example.schedulemanager.mapper.UserMapper;
import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.model.FriendRequestInfo;
import com.example.schedulemanager.model.FriendUser;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FriendshipService {
    private final FriendshipMapper friendshipMapper;
    private final UserMapper userMapper;

    public FriendshipService(FriendshipMapper friendshipMapper, UserMapper userMapper) {
        this.friendshipMapper = friendshipMapper;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<FriendUser> listFriends(Long userId) {
        return friendshipMapper.findFriends(userId);
    }

    @Transactional(readOnly = true)
    public List<FriendRequestInfo> listIncomingPending(Long userId) {
        return friendshipMapper.findIncomingPending(userId);
    }

    @Transactional(readOnly = true)
    public List<FriendRequestInfo> listOutgoingPending(Long userId) {
        return friendshipMapper.findOutgoingPending(userId);
    }

    @Transactional
    public void createRequest(Long currentUserId, FriendRequestCreateRequest request) {
        if (request == null || normalize(request.getUsername()) == null) {
            throw new IllegalArgumentException("申請先ユーザー名を指定してください。");
        }

        String targetUsername = normalize(request.getUsername());
        AppUser targetUser = userMapper.findByUsername(targetUsername);
        if (targetUser == null) {
            throw new NoSuchElementException("指定したユーザーが見つかりません。");
        }
        if (currentUserId.equals(targetUser.getId())) {
            throw new IllegalArgumentException("自分自身にはフレンド申請できません。");
        }

        int relationshipCount = friendshipMapper.countRelationship(currentUserId, targetUser.getId());
        if (relationshipCount > 0) {
            throw new IllegalArgumentException("すでにフレンド関係または申請が存在します。");
        }

        friendshipMapper.createRequest(currentUserId, targetUser.getId(), "PENDING");
    }

    @Transactional
    public void acceptRequest(Long currentUserId, Long requestId) {
        int updatedCount = friendshipMapper.acceptRequest(requestId, currentUserId);
        if (updatedCount == 0) {
            throw new NoSuchElementException("承認対象の申請が見つかりません。");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        return trimmed;
    }
}
