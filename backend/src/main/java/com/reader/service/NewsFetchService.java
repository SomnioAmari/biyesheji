package com.reader.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reader.common.BizException;
import com.reader.entity.Article;
import com.reader.entity.FetchLog;
import com.reader.entity.NewsSource;
import com.reader.mapper.ArticleMapper;
import com.reader.mapper.FetchLogMapper;
import com.reader.mapper.NewsSourceMapper;
import com.reader.parse.HtmlCleaner;
import com.reader.parse.HttpFetcher;
import com.reader.parse.RssItem;
import com.reader.parse.RssParser;
import com.reader.rule.WebSourceRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 新闻抓取：RSS / 网页源 → 解析 → 正文清洗 → 幂等入库 → 刷新缓存。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFetchService {

    private static final Safelist CONTENT_SAFELIST = Safelist.none()
            .addTags("p", "div", "span", "h1", "h2", "h3", "h4", "h5", "h6", "br", "hr",
                    "img", "a", "blockquote", "pre", "code", "ul", "ol", "li", "strong", "em",
                    "b", "i", "u", "table", "thead", "tbody", "tr", "td", "th")
            .addAttributes("a", "href")
            .addAttributes("img", "src", "alt")
            .addProtocols("a", "href", "http", "https")
            .addProtocols("img", "src", "http", "https");

    private final NewsSourceMapper sourceMapper;
    private final ArticleMapper articleMapper;
    private final FetchLogMapper fetchLogMapper;
    private final RssParser rssParser;
    private final HtmlCleaner htmlCleaner;
    private final HttpFetcher httpFetcher;
    private final NewsCacheService newsCacheService;
    private final ObjectMapper objectMapper;

    @Value("${app.fetch.max-articles-per-source:50}")
    private int maxArticles;

    public FetchLog fetchSource(NewsSource source) {
        LocalDateTime started = LocalDateTime.now();
        FetchLog fetchLog = new FetchLog();
        fetchLog.setSourceType(0);
        fetchLog.setSourceId(source.getId());
        fetchLog.setStartedAt(started);
        int count = 0;
        try {
            count = source.getType() == 0 ? fetchRss(source) : fetchWeb(source);
            fetchLog.setStatus(1);
            fetchLog.setFetchedCount(count);
            fetchLog.setMessage("成功");
            sourceMapper.updateLastFetchAt(source.getId(), started);
            newsCacheService.refresh(source.getUserId());
        } catch (Exception e) {
            log.error("抓取源 [{}] 失败", source.getName(), e);
            fetchLog.setStatus(0);
            fetchLog.setFetchedCount(0);
            fetchLog.setMessage(truncate(e.getMessage(), 400));
        } finally {
            fetchLog.setFinishedAt(LocalDateTime.now());
            fetchLogMapper.insert(fetchLog);
        }
        return fetchLog;
    }

    private int fetchRss(NewsSource source) {
        String xml = httpFetcher.get(source.getUrl(), "UTF-8");
        List<RssItem> items = rssParser.parse(xml);
        int count = 0;
        for (RssItem item : items) {
            if (item.link() == null) {
                continue;
            }
            if (count >= maxArticles) {
                break;
            }
            if (insertArticle(source, item.title(), item.link(), item.author(), item.pubDate(), item.description())) {
                count++;
            }
        }
        return count;
    }

    private int fetchWeb(NewsSource source) {
        String listHtml = httpFetcher.get(source.getUrl(), "UTF-8");
        Document doc = Jsoup.parse(listHtml, source.getUrl());
        WebSourceRule rule = parseWebRule(source.getRuleJson());

        List<String> links = new ArrayList<>();
        String listSelector = rule.getListSelector();
        if (listSelector != null && !listSelector.isBlank()) {
            for (Element a : doc.select(listSelector)) {
                collectLink(links, a.absUrl("href"));
            }
        } else {
            for (Element a : doc.select("a[href]")) {
                collectLink(links, a.absUrl("href"));
            }
        }

        Set<String> seen = new HashSet<>();
        int count = 0;
        for (String link : links) {
            if (count >= maxArticles) {
                break;
            }
            if (!seen.add(link)) {
                continue;
            }
            try {
                if (fetchWebArticle(source, link, rule)) {
                    count++;
                }
            } catch (Exception e) {
                log.warn("抓取网页文章 {} 失败: {}", link, e.getMessage());
            }
        }
        return count;
    }

    private boolean fetchWebArticle(NewsSource source, String url, WebSourceRule rule) {
        String html = httpFetcher.get(url, "UTF-8");
        Document doc = Jsoup.parse(html, url);

        String title = selectText(doc, rule.getTitleSelector());
        if (title == null || title.isBlank()) {
            title = doc.title();
        }
        String author = selectText(doc, rule.getAuthorSelector());

        String content;
        String contentSelector = rule.getContentSelector();
        if (contentSelector != null && !contentSelector.isBlank()) {
            Elements els = doc.select(contentSelector);
            content = els.isEmpty()
                    ? htmlCleaner.extractContent(html, url)
                    : Jsoup.clean(els.first().html(), url, CONTENT_SAFELIST);
        } else {
            content = htmlCleaner.extractContent(html, url);
        }
        return saveArticle(source, title, author, null, url, content, null);
    }

    private boolean insertArticle(NewsSource source, String title, String url,
                                  String author, LocalDateTime published, String summaryHtml) {
        String content;
        try {
            String detailHtml = httpFetcher.get(url, "UTF-8");
            content = htmlCleaner.extractContent(detailHtml, url);
        } catch (Exception e) {
            log.warn("抓取正文 {} 失败，使用摘要兜底", url);
            content = summaryHtml == null ? "" : summaryHtml;
        }
        String summary = summaryHtml == null ? null : htmlCleaner.summary(summaryHtml);
        return saveArticle(source, title, author, published, url, content, summary);
    }

    private boolean saveArticle(NewsSource source, String title, String author,
                                LocalDateTime published, String url, String content, String summary) {
        if (title == null || title.isBlank() || url == null || url.isBlank()) {
            return false;
        }
        String hash = sha256(url);
        if (articleMapper.findByUrlHash(hash) != null) {
            return false;
        }
        Article article = new Article();
        article.setSourceId(source.getId());
        article.setTitle(title.trim());
        article.setAuthor(author);
        article.setSourceName(source.getName());
        article.setUrl(url);
        article.setUrlHash(hash);
        article.setSummary(summary);
        article.setContent(content);
        article.setPublishedAt(published);
        articleMapper.insert(article);
        return true;
    }

    private void collectLink(List<String> links, String href) {
        if (href != null && !href.isBlank() && (href.startsWith("http://") || href.startsWith("https://"))) {
            links.add(href);
        }
    }

    private String selectText(Document doc, String selector) {
        if (selector == null || selector.isBlank()) {
            return null;
        }
        Elements els = doc.select(selector);
        return els.isEmpty() ? null : els.first().text().trim();
    }

    private WebSourceRule parseWebRule(String ruleJson) {
        if (ruleJson == null || ruleJson.isBlank()) {
            return new WebSourceRule();
        }
        try {
            return objectMapper.readValue(ruleJson, WebSourceRule.class);
        } catch (Exception e) {
            throw new BizException("网页源规则解析失败：" + e.getMessage());
        }
    }

    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new BizException("哈希计算失败");
        }
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }
}
