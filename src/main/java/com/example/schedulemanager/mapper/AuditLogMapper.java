package com.example.schedulemanager.mapper;

import com.example.schedulemanager.model.AuditLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface AuditLogMapper {

    @Insert("""
            INSERT INTO audit_log (event_type, status, message)
            VALUES (#{eventType}, #{status}, #{message})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditLog log);
}
