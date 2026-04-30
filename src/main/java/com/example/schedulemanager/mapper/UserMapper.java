package com.example.schedulemanager.mapper;

import com.example.schedulemanager.model.AppUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Select("""
            SELECT id, username, password_hash, display_name, profile_bio, profile_image_url, enabled, created_at
            FROM app_user
            WHERE username = #{username}
            """)
    AppUser findByUsername(String username);

    @Select("""
            SELECT id, username, password_hash, display_name, profile_bio, profile_image_url, enabled, created_at
            FROM app_user
            WHERE id = #{id}
            """)
    AppUser findById(Long id);

    @Insert("""
            INSERT INTO app_user (username, password_hash, display_name, profile_bio, profile_image_url, enabled)
            VALUES (#{username}, #{passwordHash}, #{displayName}, #{profileBio}, #{profileImageUrl}, #{enabled})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AppUser user);

    @Update("""
            UPDATE app_user
            SET display_name = #{displayName},
                profile_bio = #{profileBio},
                profile_image_url = #{profileImageUrl}
            WHERE id = #{id}
            """)
    int updateProfile(AppUser user);
}
