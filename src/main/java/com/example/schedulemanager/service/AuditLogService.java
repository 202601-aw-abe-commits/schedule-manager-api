package com.example.schedulemanager.service;

import com.example.schedulemanager.mapper.AuditLogMapper;
import com.example.schedulemanager.model.AuditLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void record(String eventType, String status, String message) {
        AuditLog log = new AuditLog();
        log.setEventType(eventType);
        log.setStatus(status);
        log.setMessage(message);
        auditLogMapper.insert(log);
    }
}
