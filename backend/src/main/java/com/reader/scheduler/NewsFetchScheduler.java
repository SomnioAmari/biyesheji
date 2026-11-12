package com.reader.scheduler;

import com.reader.entity.NewsSource;
import com.reader.mapper.NewsSourceMapper;
import com.reader.service.NewsFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 新闻定时批量抓取：每分钟扫描一次，抓取"到期且启用"的新闻源。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewsFetchScheduler {

    private final NewsSourceMapper sourceMapper;
    private final NewsFetchService fetchService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 10_000)
    public void fetchDueSources() {
        try {
            List<NewsSource> sources = sourceMapper.findAllEnabled();
            if (sources.isEmpty()) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            for (NewsSource source : sources) {
                try {
                    if (source.getLastFetchAt() == null
                            || Duration.between(source.getLastFetchAt(), now).toMinutes() >= source.getFetchInterval()) {
                        fetchService.fetchSource(source);
                    }
                } catch (Exception e) {
                    log.warn("定时抓取源 [{}] 异常: {}", source.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("定时抓取扫描失败（数据库不可用？）: {}", e.getMessage());
        }
    }
}
