package com.reader.service;

import com.reader.entity.Article;
import com.reader.entity.UserBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RankServiceTest {

    private final RankService rankService = new RankService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rankService, "alpha", 0.5);
        ReflectionTestUtils.setField(rankService, "beta", 0.3);
        ReflectionTestUtils.setField(rankService, "gamma", 0.2);
        ReflectionTestUtils.setField(rankService, "halfLifeHours", 24.0);
    }

    @Test
    void higherSourceWeightShouldRankHigher() {
        Article article = new Article();
        article.setPublishedAt(LocalDateTime.now());

        double high = rankService.score(article, 10, null);
        double low = rankService.score(article, 1, null);

        assertThat(high).isGreaterThan(low);
    }

    @Test
    void readCompleteBehaviorShouldBoostScore() {
        Article article = new Article();
        article.setPublishedAt(LocalDateTime.now());

        UserBehavior completed = new UserBehavior();
        completed.setClickCount(0);
        completed.setReadComplete(1);

        double withBehavior = rankService.score(article, 5, completed);
        double withoutBehavior = rankService.score(article, 5, null);

        assertThat(withBehavior).isGreaterThan(withoutBehavior);
    }

    @Test
    void newerArticleShouldRankHigher() {
        Article recent = new Article();
        recent.setPublishedAt(LocalDateTime.now());

        Article old = new Article();
        old.setPublishedAt(LocalDateTime.now().minusHours(48));

        assertThat(rankService.score(recent, 5, null))
                .isGreaterThan(rankService.score(old, 5, null));
    }
}
