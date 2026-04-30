package com.example.schedulemanager.service;

import com.example.schedulemanager.dto.RegisterRequest;
import com.example.schedulemanager.dto.ProfileUpdateRequest;
import com.example.schedulemanager.mapper.UserMapper;
import com.example.schedulemanager.model.AppUser;
import java.util.NoSuchElementException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AppUser getByUsername(String username) {
        AppUser user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("ユーザーが存在しません。");
        }
        return user;
    }

    @Transactional
    public AppUser register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("登録情報が空です。");
        }

        String username = normalize(request.getUsername());
        String password = normalize(request.getPassword());
        String displayName = normalize(request.getDisplayName());

        validateUsername(username);
        validatePassword(password);

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("表示名は必須です。");
        }
        if (displayName.length() > 100) {
            throw new IllegalArgumentException("表示名は100文字以内で入力してください。");
        }

        if (userMapper.findByUsername(username) != null) {
            throw new IllegalArgumentException("そのユーザー名はすでに使われています。");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(displayName);
        user.setEnabled(true);
        userMapper.insert(user);
        return userMapper.findById(user.getId());
    }

    @Transactional
    public AppUser updateProfile(String username, ProfileUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("更新情報が空です。");
        }

        AppUser user = userMapper.findByUsername(username);
        if (user == null) {
            throw new NoSuchElementException("ユーザーが見つかりません。");
        }

        String displayName = normalize(request.getDisplayName());
        String profileBio = normalize(request.getProfileBio());
        String profileImageUrl = normalize(request.getProfileImageUrl());

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("表示名は必須です。");
        }
        if (displayName.length() > 100) {
            throw new IllegalArgumentException("表示名は100文字以内で入力してください。");
        }
        if (profileBio != null && profileBio.length() > 500) {
            throw new IllegalArgumentException("自己紹介は500文字以内で入力してください。");
        }
        if (profileImageUrl != null && profileImageUrl.length() > 1000) {
            throw new IllegalArgumentException("アイコンURLは1000文字以内で入力してください。");
        }

        user.setDisplayName(displayName);
        user.setProfileBio(profileBio);
        user.setProfileImageUrl(profileImageUrl);
        userMapper.updateProfile(user);
        return userMapper.findById(user.getId());
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("ユーザー名は必須です。");
        }
        if (!username.matches("^[a-zA-Z0-9_]{4,30}$")) {
            throw new IllegalArgumentException("ユーザー名は4〜30文字の英数字/アンダースコアで入力してください。");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("パスワードは必須です。");
        }
        if (password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("パスワードは8〜72文字で入力してください。");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}
