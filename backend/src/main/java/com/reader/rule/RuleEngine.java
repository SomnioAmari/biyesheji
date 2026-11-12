package com.reader.rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reader.common.BizException;
import com.reader.entity.BookSource;
import com.reader.parse.HttpFetcher;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RuleEngine {

    private final HttpFetcher httpFetcher;
    private final ObjectMapper objectMapper;

    public BookSourceRule parse(String ruleJson) {
        try {
            return objectMapper.readValue(ruleJson, BookSourceRule.class);
        } catch (Exception e) {
            throw new BizException("书源规则 JSON 解析失败：" + e.getMessage());
        }
    }

    public List<SearchItem> search(BookSource source, String keyword) {
        BookSourceRule rule = parse(source.getRuleJson());
        String encodedKey = URLEncoder.encode(keyword == null ? "" : keyword, StandardCharsets.UTF_8);
        Map<String, String> kv = Map.of("key", encodedKey, "page", "1");
        String searchUrl = buildUrl(rule.getUrl(), render(rule.getSearchUrl(), kv));
        String html;
        if ("POST".equalsIgnoreCase(rule.getSearchMethod())) {
            String body = render(rule.getSearchBody(), kv);
            html = httpFetcher.post(searchUrl, body == null ? "" : body, rule.getCharset(), buildHeaders(rule));
        } else {
            html = httpFetcher.get(searchUrl, rule.getCharset(), buildHeaders(rule));
        }
        Document doc = Jsoup.parse(html, searchUrl);

        Elements list = select(doc, rule.getSearchList());
        List<SearchItem> results = new ArrayList<>();
        for (Element item : list) {
            String name = firstText(item, rule.getSearchName(), rule.getSearchNameType());
            String author = firstText(item, rule.getSearchAuthor(), rule.getSearchAuthorType());
            String cover = firstAttr(item, rule.getSearchCover(), "src");
            String detailUrl = firstAttr(item, rule.getSearchDetail(), "href");
            if (name == null || detailUrl == null) {
                continue;
            }
            detailUrl = absolutize(searchUrl, detailUrl);
            cover = cover == null ? null : absolutize(searchUrl, cover);
            results.add(new SearchItem(name, author, cover, detailUrl));
        }
        return results;
    }

    public RuleBookDetail detail(BookSource source, String bookUrl) {
        BookSourceRule rule = parse(source.getRuleJson());
        Document doc = Jsoup.parse(httpFetcher.get(bookUrl, rule.getCharset(), buildHeaders(rule)), bookUrl);

        String name = firstText(doc, rule.getDetailName(), rule.getDetailNameType());
        String author = firstText(doc, rule.getDetailAuthor(), rule.getDetailAuthorType());
        String cover = firstAttr(doc, rule.getDetailCover(), "src");
        cover = cover == null ? null : absolutize(bookUrl, cover);
        String intro = firstText(doc, rule.getDetailIntro(), rule.getDetailIntroType());

        String bookId = extractBookId(bookUrl, rule.getBookUrlPattern());
        Map<String, String> vars = Map.of(
                "bookId", bookId == null ? "" : bookId,
                "bookUrl", bookUrl);
        String tocUrl = render(rule.getTocUrl(), vars);
        tocUrl = buildUrl(rule.getUrl(), tocUrl);

        return new RuleBookDetail(name, author, cover, intro, tocUrl);
    }

    public List<ChapterItem> catalog(BookSource source, String tocUrl) {
        BookSourceRule rule = parse(source.getRuleJson());
        Document doc = Jsoup.parse(httpFetcher.get(tocUrl, rule.getCharset(), buildHeaders(rule)), tocUrl);

        Elements list = select(doc, rule.getCatalogList());
        List<ChapterItem> chapters = new ArrayList<>();
        for (Element item : list) {
            String name = firstText(item, rule.getCatalogName());
            String url = firstAttr(item, rule.getCatalogUrl(), "href");
            if (name == null || url == null) {
                continue;
            }
            chapters.add(new ChapterItem(name, absolutize(tocUrl, url)));
        }
        return chapters;
    }

    public String content(BookSource source, String chapterUrl) {
        BookSourceRule rule = parse(source.getRuleJson());
        String fetchUrl = render(rule.getContentUrl(), Map.of("chapterUrl", chapterUrl));
        if (fetchUrl == null || fetchUrl.isBlank()) {
            fetchUrl = chapterUrl;
        }
        fetchUrl = buildUrl(rule.getUrl(), fetchUrl);
        Document doc = Jsoup.parse(httpFetcher.get(fetchUrl, rule.getCharset(), buildHeaders(rule)), fetchUrl);
        Elements els = select(doc, rule.getBookContent());
        if (els.isEmpty()) {
            return "";
        }
        return els.first().wholeText().trim();
    }

    private Map<String, String> buildHeaders(BookSourceRule rule) {
        Map<String, String> headers = new HashMap<>();
        if (rule.getHeaders() != null) {
            headers.putAll(rule.getHeaders());
        }
        if (rule.getUserAgent() != null && !rule.getUserAgent().isBlank()) {
            headers.put("User-Agent", rule.getUserAgent());
        }
        return headers;
    }

    // ---------- helpers ----------

    private String render(String template, Map<String, String> vars) {
        if (template == null) {
            return null;
        }
        String out = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            out = out.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }

    private String buildUrl(String base, String path) {
        if (path == null || path.isBlank()) {
            return base;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        try {
            return new URI(base).resolve(path).toString();
        } catch (Exception e) {
            return path;
        }
    }

    private String absolutize(String pageUrl, String href) {
        if (href == null || href.isBlank()) {
            return null;
        }
        if (href.startsWith("http://") || href.startsWith("https://")) {
            return href;
        }
        try {
            return new URI(pageUrl).resolve(href).toString();
        } catch (Exception e) {
            return href;
        }
    }

    private String extractBookId(String bookUrl, String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return null;
        }
        Matcher m = Pattern.compile(pattern).matcher(bookUrl);
        if (m.find()) {
            return m.groupCount() >= 1 ? m.group(1) : m.group();
        }
        return null;
    }

    private Elements select(Element ctx, String rule) {
        if (rule == null || rule.isBlank()) {
            return new Elements();
        }
        String r = rule.trim();
        if (r.startsWith("css:")) {
            return ctx.select(r.substring(4));
        }
        if (r.startsWith("//") || r.startsWith(".//")) {
            try {
                return ctx.selectXpath(r);
            } catch (Exception e) {
                return new Elements();
            }
        }
        return ctx.select(r);
    }

    private String firstText(Element ctx, String rule) {
        return firstText(ctx, rule, null);
    }

    private String firstText(Element ctx, String rule, String type) {
        Elements els = select(ctx, rule);
        if (els.isEmpty()) {
            return null;
        }
        Element e = els.first();
        String t;
        if ("title".equals(type)) {
            t = e.attr("title");
        } else if ("alt".equals(type)) {
            t = e.attr("alt");
        } else if ("content".equals(type)) {
            t = e.attr("content");
        } else if ("html".equals(type)) {
            t = e.html();
        } else if ("ownText".equals(type)) {
            t = e.ownText();
        } else if ("textNodes".equals(type)) {
            t = e.wholeText();
        } else {
            t = e.text();
        }
        t = t == null ? null : t.trim();
        return t == null || t.isEmpty() ? null : t;
    }

    private String firstAttr(Element ctx, String rule, String attr) {
        Elements els = select(ctx, rule);
        if (els.isEmpty()) {
            return null;
        }
        String v = els.first().attr(attr).trim();
        return v.isEmpty() ? null : v;
    }
}
