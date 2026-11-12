package com.reader.service;

import com.reader.entity.Article;
import com.reader.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 新闻主页候选集缓存：Redis ZSET，member=articleId，score=发布时间戳。
 * Redis 不可用时自动降级（返回空，由业务层回源 MySQL）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewsCacheService {

    private static final String KEY_PREFIX = "feed:candidates:";
    private static final int CACHE_SIZE = 500;

    private final StringRedisTemplate redis;
    private final ArticleMapper articleMapper;

    public void refresh(Long userId) {
        try {
            List<Article> articles = articleMapper.findRecentByUser(userId, CACHE_SIZE);
            String key = key(userId);
            redis.delete(key);
            if (articles.isEmpty()) {
                return;
            }
            Set<ZSetOperations.TypedTuple<String>> tuples = articles.stream()
                    .map(a -> new DefaultTypedTuple<>(String.valueOf(a.getId()),
                            a.getPublishedAt() == null ? 0D
                                    : a.getPublishedAt().atZone(ZoneId.systemDefault()).toEpochSecond()))
                    .collect(Collectors.toSet());
            redis.opsForZSet().add(key, tuples);
            redis.expire(key, Duration.ofHours(6));
        } catch (Exception e) {
            log.warn("Redis 缓存刷新失败，降级为直接查库: {}", e.getMessage());
        }
    }

    public List<Long> getCandidateIds(Long userId) {
        try {
            Set<String> ids = redis.opsForZSet().reverseRange(key(userId), 0, CACHE_SIZE - 1);
            if (ids == null || ids.isEmpty()) {
                return Collections.emptyList();
            }
            return ids.stream().map(Long::valueOf).toList();
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级为直接查库: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
