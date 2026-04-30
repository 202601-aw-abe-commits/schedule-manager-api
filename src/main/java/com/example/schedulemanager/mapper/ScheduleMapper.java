package com.example.schedulemanager.mapper;

import com.example.schedulemanager.model.ScheduleItem;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ScheduleMapper {

    @Select("""
            SELECT s.id, s.owner_user_id, u.username AS owner_username, u.display_name AS owner_display_name,
                   s.schedule_date, s.title, s.start_time, s.end_time, s.description, s.shared_with_friends,
                   s.created_at, s.updated_at
            FROM schedule_item s
            LEFT JOIN app_user u ON u.id = s.owner_user_id
            WHERE s.schedule_date = #{date}
              AND (
                  s.owner_user_id = #{viewerUserId}
                  OR (
                      s.shared_with_friends = TRUE
                      AND EXISTS (
                          SELECT 1
                          FROM friendship f
                          WHERE f.status = 'ACCEPTED'
                            AND (
                                (f.requester_user_id = s.owner_user_id AND f.addressee_user_id = #{viewerUserId})
                                OR (f.requester_user_id = #{viewerUserId} AND f.addressee_user_id = s.owner_user_id)
                            )
                      )
                  )
              )
            ORDER BY COALESCE(s.start_time, TIME '23:59:59'), s.id
            """)
    List<ScheduleItem> findVisibleByDate(@Param("date") LocalDate date, @Param("viewerUserId") Long viewerUserId);

    @Select("""
            SELECT s.id, s.owner_user_id, u.username AS owner_username, u.display_name AS owner_display_name,
                   s.schedule_date, s.title, s.start_time, s.end_time, s.description, s.shared_with_friends,
                   s.created_at, s.updated_at
            FROM schedule_item s
            LEFT JOIN app_user u ON u.id = s.owner_user_id
            WHERE s.id = #{id}
              AND (
                  s.owner_user_id = #{viewerUserId}
                  OR (
                      s.shared_with_friends = TRUE
                      AND EXISTS (
                          SELECT 1
                          FROM friendship f
                          WHERE f.status = 'ACCEPTED'
                            AND (
                                (f.requester_user_id = s.owner_user_id AND f.addressee_user_id = #{viewerUserId})
                                OR (f.requester_user_id = #{viewerUserId} AND f.addressee_user_id = s.owner_user_id)
                            )
                      )
                  )
              )
            """)
    ScheduleItem findVisibleById(@Param("id") Long id, @Param("viewerUserId") Long viewerUserId);

    @Select("""
            SELECT s.id, s.owner_user_id, s.schedule_date, s.title, s.start_time, s.end_time, s.description,
                   s.shared_with_friends, s.created_at, s.updated_at
            FROM schedule_item s
            WHERE s.id = #{id}
              AND s.owner_user_id = #{ownerUserId}
            """)
    ScheduleItem findOwnedById(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId);

    @Insert("""
            INSERT INTO schedule_item (owner_user_id, schedule_date, title, start_time, end_time, description, shared_with_friends)
            VALUES (#{ownerUserId}, #{scheduleDate}, #{title}, #{startTime}, #{endTime}, #{description}, #{sharedWithFriends})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ScheduleItem item);

    @Update("""
            UPDATE schedule_item
            SET schedule_date = #{scheduleDate},
                title = #{title},
                start_time = #{startTime},
                end_time = #{endTime},
                description = #{description},
                shared_with_friends = #{sharedWithFriends},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
              AND owner_user_id = #{ownerUserId}
            """)
    int update(ScheduleItem item);

    @Delete("""
            DELETE FROM schedule_item
            WHERE id = #{id}
              AND owner_user_id = #{ownerUserId}
            """)
    int delete(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId);
}
