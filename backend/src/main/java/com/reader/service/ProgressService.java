package com.reader.service;

import com.reader.dto.ProgressRequest;
import com.reader.entity.ReadingProgress;
import com.reader.entity.UserBehavior;
import com.reader.mapper.ReadingProgressMapper;
import com.reader.mapper.UserBehaviorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private static final BigDecimal COMPLETE_THRESHOLD = new BigDecimal("0.9");

    private final ReadingProgressMapper progressMapper;
    private final UserBehaviorMapper behaviorMapper;

    public ReadingProgress get(Long userId, int targetType, Long targetId) {
        return progressMapper.findByUserAndTarget(userId, targetType, targetId);
    }

    public void report(Long userId, ProgressRequest req) {
        ReadingProgress p = new ReadingProgress();
        p.setUserId(userId);
        p.setTargetType(req.getTargetType());
        p.setTargetId(req.getTargetId());
        p.setChapterId(req.getChapterId());
        p.setScrollPosition(req.getScrollPosition());
        progressMapper.upsert(p);

        // 文章读到底(>=90%)时标记读完，供个性化排序
        if (req.getTargetType() == 0 && req.getScrollPosition() != null
                && req.getScrollPosition().compareTo(COMPLETE_THRESHOLD) >= 0) {
            UserBehavior b = new UserBehavior();
            b.setUserId(userId);
            b.setTargetType(0);
            b.setTargetId(req.getTargetId());
            b.setClickCount(0);
            b.setReadDuration(0);
            b.setReadComplete(1);
            behaviorMapper.upsert(b);
        }
    }
}
