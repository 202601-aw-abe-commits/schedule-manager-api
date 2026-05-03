package com.example.schedulemanager.mapper;

import com.example.schedulemanager.model.ScheduleHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface ScheduleHistoryMapper {

    @Insert("""
            INSERT INTO schedule_history (schedule_id, action_type, action_detail)
            VALUES (#{scheduleId}, #{actionType}, #{actionDetail})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ScheduleHistory history);
}
