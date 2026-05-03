package com.example.schedulemanager.mapper;

import com.example.schedulemanager.model.TodoHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface TodoHistoryMapper {

    @Insert("""
            INSERT INTO todo_history (todo_id, action_type, action_detail)
            VALUES (#{todoId}, #{actionType}, #{actionDetail})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TodoHistory history);
}
