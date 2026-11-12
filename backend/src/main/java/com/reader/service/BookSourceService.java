package com.reader.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reader.common.BizException;
import com.reader.dto.BookSourceRequest;
import com.reader.entity.BookSource;
import com.reader.mapper.BookSourceMapper;
import com.reader.parse.HttpFetcher;
import com.reader.rule.BookSourceRule;
import com.reader.rule.LegadoRuleConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookSourceService {

    private final BookSourceMapper mapper;
    private final ObjectMapper objectMapper;
    private final HttpFetcher httpFetcher;
    private final LegadoRuleConverter legadoRuleConverter;

    public List<BookSource> list(Long userId) {
        return mapper.findByUser(userId);
    }

    public BookSource create(Long userId, BookSourceRequest req) {
        BookSource source = new BookSource();
        source.setUserId(userId);
        apply(source, req);
        mapper.insert(source);
        return source;
    }

    public BookSource update(Long userId, Long id, BookSourceRequest req) {
        BookSource source = requireOwned(userId, id);
        apply(source, req);
        mapper.update(source);
        return source;
    }

    public void delete(Long userId, Long id) {
        requireOwned(userId, id);
        mapper.delete(id);
    }

    /**
     * 从 URL 抓取 JSON 并导入书源（支持单个书源或书源合集数组，支持 legado 格式与本系统原生格式）。
     */
    public List<BookSource> importFromUrl(Long userId, String url) {
        String jsonText = httpFetcher.get(url, "UTF-8");
        return importJson(userId, jsonText);
    }

    /**
     * 解析书源 JSON 文本并导入（支持单个对象或数组，自动识别 legado 格式）。
     */
    public List<BookSource> importJson(Long userId, String jsonText) {
        JsonNode root;
        try {
            root = objectMapper.readTree(jsonText);
        } catch (Exception e) {
            throw new BizException("书源 JSON 解析失败：" + e.getMessage());
        }

        List<JsonNode> nodes = new ArrayList<>();
        if (root.isArray()) {
            root.forEach(nodes::add);
        } else {
            nodes.add(root);
        }

        List<BookSource> imported = new ArrayList<>();
        for (JsonNode node : nodes) {
            BookSource source = convertNode(userId, node);
            mapper.insert(source);
            imported.add(source);
        }
        return imported;
    }

    /**
     * 对已导入书源的规则重新解析转换（用于升级转换器后修复旧数据）。
     */
    public int reconvertAll(Long userId) {
        List<BookSource> sources = mapper.findByUser(userId);
        int count = 0;
        for (BookSource source : sources) {
            try {
                BookSourceRule rule = objectMapper.readValue(source.getRuleJson(), BookSourceRule.class);
                legadoRuleConverter.reconvert(rule);
                source.setRuleJson(objectMapper.writeValueAsString(rule));
                mapper.update(source);
                count++;
            } catch (Exception e) {
                log.warn("重转换书源 [{}] 失败: {}", source.getName(), e.getMessage());
            }
        }
        return count;
    }

    private BookSource convertNode(Long userId, JsonNode node) {
        BookSourceRule rule;
        try {
            rule = legadoRuleConverter.isLegadoFormat(node)
                    ? legadoRuleConverter.convert(node)
                    : objectMapper.treeToValue(node, BookSourceRule.class);
        } catch (Exception e) {
            throw new BizException("书源规则转换失败：" + e.getMessage());
        }
        if (rule.getName() == null || rule.getName().isBlank()
                || rule.getUrl() == null || rule.getUrl().isBlank()) {
            throw new BizException("书源缺少名称或地址");
        }

        BookSource source = new BookSource();
        source.setUserId(userId);
        source.setName(rule.getName());
        source.setUrl(rule.getUrl());
        try {
            source.setRuleJson(objectMapper.writeValueAsString(rule));
        } catch (Exception e) {
            throw new BizException("书源规则序列化失败");
        }
        source.setEnabled(1);
        return source;
    }

    private BookSource requireOwned(Long userId, Long id) {
        BookSource source = mapper.findById(id);
        if (source == null || !source.getUserId().equals(userId)) {
            throw new BizException(403, "无权操作该书源");
        }
        return source;
    }

    private void apply(BookSource s, BookSourceRequest req) {
        s.setName(req.getName());
        s.setUrl(req.getUrl());
        s.setRuleJson(req.getRuleJson());
        s.setEnabled(req.getEnabled() == null ? 1 : req.getEnabled());
    }
}
