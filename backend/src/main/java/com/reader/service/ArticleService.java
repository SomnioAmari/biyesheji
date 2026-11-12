package com.reader.service;

import com.reader.common.BizException;
import com.reader.common.PageResult;
import com.reader.entity.Article;
import com.reader.entity.ArticleFavorite;
import com.reader.entity.NewsSource;
import com.reader.entity.ReadingProgress;
import com.reader.entity.UserBehavior;
import com.reader.mapper.ArticleFavoriteMapper;
import com.reader.mapper.ArticleMapper;
import com.reader.mapper.NewsSourceMapper;
import com.reader.mapper.ReadingProgressMapper;
import com.reader.mapper.UserBehaviorMapper;
import com.reader.vo.ArticleDetailVO;
import com.reader.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final NewsSourceMapper newsSourceMapper;
    private final UserBehaviorMapper userBehaviorMapper;
    private final ArticleFavoriteMapper favoriteMapper;
    private final ReadingProgressMapper progressMapper;
    private final NewsCacheService newsCacheService;
    private final RankService rankService;

    public PageResult<ArticleVO> feed(Long userId, Long sourceId, int page, int size) {
        List<Article> candidates;
        List<Long> ids = newsCacheService.getCandidateIds(userId);
        if (!ids.isEmpty()) {
            List<Article> byIds = articleMapper.findByIds(ids).stream()
                    .filter(Objects::nonNull).collect(Collectors.toList());
            if (sourceId != null) {
                byIds = byIds.stream().filter(a -> sourceId.equals(a.getSourceId())).toList();
            }
            candidates = byIds;
        } else {
            candidates = articleMapper.findRecentByUserAndSource(userId, sourceId, 500);
        }

        Map<Long, Integer> weights = newsSourceMapper.findByUser(userId).stream()
                .collect(Collectors.toMap(NewsSource::getId,
                        s -> s.getWeight() == null ? 5 : s.getWeight(), (a, b) -> a));
        Map<Long, UserBehavior> behaviors = userBehaviorMapper.findByUserAndType(userId, 0).stream()
                .collect(Collectors.toMap(UserBehavior::getTargetId, b -> b, (a, b) -> a));
        Set<Long> favIds = favoriteMapper.findByUser(userId).stream()
                .map(ArticleFavorite::getArticleId).collect(Collectors.toSet());

        List<Article> sorted = new ArrayList<>(candidates);
        sorted.sort((x, y) -> Double.compare(
                rankService.score(y, weights.getOrDefault(y.getSourceId(), 5), behaviors.get(y.getId())),
                rankService.score(x, weights.getOrDefault(x.getSourceId(), 5), behaviors.get(x.getId()))
        ));

        int total = sorted.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<ArticleVO> vos = new ArrayList<>();
        for (int i = from; i < to; i++) {
            Article a = sorted.get(i);
            vos.add(toVO(a, favIds.contains(a.getId())));
        }
        return PageResult.of(total, vos);
    }

    public ArticleDetailVO detail(Long userId, Long id) {
        Article article = articleMapper.findById(id);
        if (article == null) {
            throw new BizException(404, "文章不存在");
        }
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setTargetType(0);
        behavior.setTargetId(id);
        behavior.setClickCount(1);
        behavior.setReadDuration(0);
        behavior.setReadComplete(0);
        userBehaviorMapper.upsert(behavior);

        boolean fav = favoriteMapper.findByUserAndArticle(userId, id) != null;
        ReadingProgress progress = progressMapper.findByUserAndTarget(userId, 0, id);
        return new ArticleDetailVO(article.getId(), article.getTitle(), article.getAuthor(),
                article.getSourceName(), article.getCover(), article.getPublishedAt(), article.getUrl(),
                article.getContent(), fav, progress == null ? null : progress.getScrollPosition());
    }

    public void favorite(Long userId, Long articleId) {
        if (articleMapper.findById(articleId) == null) {
            throw new BizException(404, "文章不存在");
        }
        if (favoriteMapper.findByUserAndArticle(userId, articleId) != null) {
            return;
        }
        ArticleFavorite favorite = new ArticleFavorite();
        favorite.setUserId(userId);
        favorite.setArticleId(articleId);
        favoriteMapper.insert(favorite);
    }

    public void unfavorite(Long userId, Long articleId) {
        favoriteMapper.delete(userId, articleId);
    }

    public List<ArticleVO> listFavorites(Long userId) {
        List<ArticleFavorite> favorites = favoriteMapper.findByUser(userId);
        List<Long> ids = favorites.stream().map(ArticleFavorite::getArticleId).toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Article> articleMap = articleMapper.findByIds(ids).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Article::getId, a -> a, (a, b) -> a));
        return favorites.stream()
                .map(f -> articleMap.get(f.getArticleId()))
                .filter(Objects::nonNull)
                .map(a -> toVO(a, true))
                .toList();
    }

    private ArticleVO toVO(Article a, boolean favorited) {
        return new ArticleVO(a.getId(), a.getTitle(), a.getAuthor(), a.getSourceName(),
                a.getSummary(), a.getCover(), a.getPublishedAt(), a.getUrl(), favorited);
    }
}
