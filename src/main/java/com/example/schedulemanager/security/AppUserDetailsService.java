package com.example.schedulemanager.security;

import com.example.schedulemanager.mapper.UserMapper;
import com.example.schedulemanager.model.AppUser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    public AppUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("ユーザーが見つかりません: " + username);
        }

        boolean enabled = user.getEnabled() != null && user.getEnabled();
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles("USER")
                .disabled(!enabled)
                .build();
    }
}
