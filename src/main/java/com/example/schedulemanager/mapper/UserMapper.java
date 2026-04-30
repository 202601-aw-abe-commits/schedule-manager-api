package com.example.schedulemanager.mapper;

import com.example.schedulemanager.model.AppUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("""
            SELECT id, username, password_hash, display_name, enabled, created_at
            FROM app_user
            WHERE username = #{username}
            """)
    AppUser findByUsername(String username);

    @Select("""
            SELECT id, username, password_hash, display_name, enabled, created_at
            FROM app_user
            WHERE id = #{id}
            """)
    AppUser findById(Long id);

    @Insert("""
            INSERT INTO app_user (username, password_hash, display_name, enabled)
            VALUES (#{username}, #{passwordHash}, #{displayName}, #{enabled})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AppUser user);
}
