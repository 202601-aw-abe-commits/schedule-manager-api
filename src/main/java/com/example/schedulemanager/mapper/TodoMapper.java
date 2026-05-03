package com.example.schedulemanager.mapper;

import com.example.schedulemanager.model.TodoItem;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TodoMapper {

    @Insert("""
            INSERT INTO todo_item (title, completed)
            VALUES (#{title}, #{completed})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TodoItem item);

    @Update("""
            UPDATE todo_item
            SET title = #{title},
                completed = #{completed},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int update(TodoItem item);

    @Select("""
            SELECT id, title, completed, created_at, updated_at
            FROM todo_item
            WHERE id = #{id}
            """)
    TodoItem findById(Long id);

    @Select("""
            SELECT id, title, completed, created_at, updated_at
            FROM todo_item
            ORDER BY id DESC
            """)
    List<TodoItem> findAll();
}
