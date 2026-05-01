package com.example.schedulemanager.mapper;

import com.example.schedulemanager.model.PointHistory;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PointHistoryMapper {

    @Insert("""
            INSERT INTO point_history (user_id, action_type, action_label, points, action_key)
            VALUES (#{userId}, #{actionType}, #{actionLabel}, #{points}, #{actionKey})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PointHistory history);

    @Select("""
            SELECT id, user_id, action_type, action_label, points, action_key, created_at
            FROM point_history
            WHERE user_id = #{userId}
            ORDER BY created_at DESC, id DESC
            FETCH FIRST 100 ROWS ONLY
            """)
    List<PointHistory> findRecentByUserId(@Param("userId") Long userId);
}
