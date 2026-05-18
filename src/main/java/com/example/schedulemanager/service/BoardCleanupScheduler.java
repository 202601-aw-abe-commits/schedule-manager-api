package com.example.schedulemanager.service;

import com.example.schedulemanager.mapper.BoardMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardCleanupScheduler {
    private static final Logger log = LoggerFactory.getLogger(BoardCleanupScheduler.class);

    private final BoardMapper boardMapper;

    @Value("${app.board.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${app.board.cleanup.zone:Asia/Tokyo}")
    private String cleanupZone;

    public BoardCleanupScheduler(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    @Transactional
    @Scheduled(cron = "${app.board.cleanup.cron:0 5 0 * * *}", zone = "${app.board.cleanup.zone:Asia/Tokyo}")
    public void cleanupExpiredRecruitments() {
        if (!cleanupEnabled) {
            return;
        }
        LocalDate today = LocalDate.now(ZoneId.of(cleanupZone));
        int deletedPosts = boardMapper.deletePostsBeforeDate(today);
        int deletedThreads = boardMapper.deleteThreadsWithoutPosts();
        if (deletedPosts > 0 || deletedThreads > 0) {
            log.info("掲示板募集の期限切れ削除を実行しました。baseDate={}, deletedPosts={}, deletedThreads={}",
                    today, deletedPosts, deletedThreads);
        }
    }
}
