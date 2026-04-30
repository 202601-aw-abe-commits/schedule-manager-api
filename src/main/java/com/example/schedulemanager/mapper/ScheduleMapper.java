package com.example.schedulemanager.mapper;

import com.example.schedulemanager.model.ScheduleItem;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ScheduleMapper {

    @Select("""
            SELECT id, schedule_date, title, start_time, end_time, description, created_at, updated_at
            FROM schedule_item
            WHERE schedule_date = #{date}
            ORDER BY COALESCE(start_time, TIME '23:59:59'), id
            """)
    List<ScheduleItem> findByDate(LocalDate date);

    @Select("""
            SELECT id, schedule_date, title, start_time, end_time, description, created_at, updated_at
            FROM schedule_item
            WHERE id = #{id}
            """)
    ScheduleItem findById(Long id);

    @Insert("""
            INSERT INTO schedule_item (schedule_date, title, start_time, end_time, description)
            VALUES (#{scheduleDate}, #{title}, #{startTime}, #{endTime}, #{description})
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
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int update(ScheduleItem item);

    @Delete("DELETE FROM schedule_item WHERE id = #{id}")
    int delete(Long id);
}
