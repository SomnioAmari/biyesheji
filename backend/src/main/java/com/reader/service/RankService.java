package com.reader.service;

import com.reader.entity.Article;
import com.reader.entity.UserBehavior;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 新闻个性化排序打分：score = α·源权重 + β·新鲜度 + γ·行为分。
 */
@Component
public class RankService {

    @Value("${app.rank.alpha:0.5}")
    private double alpha;

    @Value("${app.rank.beta:0.3}")
    private double beta;

    @Value("${app.rank.gamma:0.2}")
    private double gamma;

    @Value("${app.rank.time-half-life-hours:24}")
    private double halfLifeHours;

    public double score(Article article, int sourceWeight, UserBehavior behavior) {
        return alpha * normalizeWeight(sourceWeight)
                + beta * timeDecay(article.getPublishedAt())
                + gamma * behaviorScore(behavior);
    }

    private double normalizeWeight(int weight) {
        return Math.min(1.0, Math.max(0.0, weight / 10.0));
    }

    private double timeDecay(LocalDateTime publishedAt) {
        if (publishedAt == null) {
            return 0;
        }
        long hours = Math.max(0, Duration.between(publishedAt, LocalDateTime.now()).toHours());
        return Math.pow(0.5, hours / halfLifeHours);
    }

    private double behaviorScore(UserBehavior b) {
        if (b == null) {
            return 0;
        }
        double s = 0;
        if (b.getClickCount() != null) {
            s += Math.min(b.getClickCount(), 10) * 0.05;
        }
        if (b.getReadComplete() != null && b.getReadComplete() > 0) {
            s += 0.5;
        }
        return Math.min(1.0, s);
    }
}
