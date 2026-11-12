package com.reader.service;

import com.reader.common.BizException;
import com.reader.dto.NewsSourceRequest;
import com.reader.entity.FetchLog;
import com.reader.entity.NewsSource;
import com.reader.mapper.NewsSourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsSourceService {

    private final NewsSourceMapper mapper;
    private final NewsFetchService fetchService;

    public List<NewsSource> list(Long userId) {
        return mapper.findByUser(userId);
    }

    public NewsSource create(Long userId, NewsSourceRequest req) {
        NewsSource source = new NewsSource();
        source.setUserId(userId);
        apply(source, req);
        mapper.insert(source);
        return source;
    }

    public NewsSource update(Long userId, Long id, NewsSourceRequest req) {
        NewsSource source = requireOwned(userId, id);
        apply(source, req);
        mapper.update(source);
        return source;
    }

    public void delete(Long userId, Long id) {
        requireOwned(userId, id);
        mapper.delete(id);
    }

    public FetchLog fetchNow(Long userId, Long id) {
        NewsSource source = requireOwned(userId, id);
        return fetchService.fetchSource(source);
    }

    private NewsSource requireOwned(Long userId, Long id) {
        NewsSource source = mapper.findById(id);
        if (source == null || !source.getUserId().equals(userId)) {
            throw new BizException(403, "无权操作该新闻源");
        }
        return source;
    }

    private void apply(NewsSource s, NewsSourceRequest req) {
        s.setType(req.getType());
        s.setName(req.getName());
        s.setUrl(req.getUrl());
        s.setCategory(req.getCategory());
        s.setRuleJson(req.getRuleJson());
        s.setWeight(req.getWeight() == null ? 5 : req.getWeight());
        s.setFetchInterval(req.getFetchInterval() == null ? 60 : req.getFetchInterval());
        s.setEnabled(req.getEnabled() == null ? 1 : req.getEnabled());
    }
}
