package com.example.schedulemanager.service;

import com.example.schedulemanager.mapper.TodoHistoryMapper;
import com.example.schedulemanager.mapper.TodoMapper;
import com.example.schedulemanager.model.TodoHistory;
import com.example.schedulemanager.model.TodoItem;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TodoService {
    private final TodoMapper todoMapper;
    private final TodoHistoryMapper todoHistoryMapper;
    private final AuditLogService auditLogService;

    public TodoService(
            TodoMapper todoMapper,
            TodoHistoryMapper todoHistoryMapper,
            AuditLogService auditLogService) {
        this.todoMapper = todoMapper;
        this.todoHistoryMapper = todoHistoryMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(rollbackFor = Exception.class)
    public TodoItem createTodo(String title) throws Exception {
        auditLogService.record("TODO_CREATE", "STARTED", "title=" + title);
        TodoItem item = new TodoItem();
        item.setTitle(normalizeTitle(title));
        item.setCompleted(false);
        todoMapper.insert(item);
        saveHistory(item.getId(), "CREATE", "todo created");
        return todoMapper.findById(item.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public TodoItem updateTodo(Long id, String title, Boolean completed) throws Exception {
        auditLogService.record("TODO_UPDATE", "STARTED", "id=" + id);
        TodoItem existing = todoMapper.findById(id);
        if (existing == null) {
            throw new NoSuchElementException("Todo not found. id=" + id);
        }
        existing.setTitle(normalizeTitle(title));
        existing.setCompleted(Boolean.TRUE.equals(completed));
        int updated = todoMapper.update(existing);
        if (updated == 0) {
            throw new NoSuchElementException("Todo not found. id=" + id);
        }
        saveHistory(id, "UPDATE", "todo updated");
        return todoMapper.findById(id);
    }

    @Transactional(readOnly = true)
    public List<TodoItem> listTodos() {
        return todoMapper.findAll();
    }

    @Transactional(readOnly = true)
    public TodoItem getTodo(Long id) {
        return todoMapper.findById(id);
    }

    private void saveHistory(Long todoId, String actionType, String actionDetail) {
        TodoHistory history = new TodoHistory();
        history.setTodoId(todoId);
        history.setActionType(actionType);
        history.setActionDetail(actionDetail);
        todoHistoryMapper.insert(history);
    }

    private String normalizeTitle(String value) throws Exception {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new Exception("title is required");
        }
        if (normalized.length() > 200) {
            throw new Exception("title must be <= 200 chars");
        }
        return normalized;
    }
}
