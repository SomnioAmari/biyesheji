package com.reader.rule;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * legado（开源阅读）书源格式转换器。
 * 把 legado 书源结构（ruleSearch / ruleBookInfo / ruleToc / ruleContent）与选择器 DSL
 * 转换成系统内部的简化 BookSourceRule。
 *
 * 支持：class./id./tag./@css:/@xpath: 选择器、.N 索引、@tag/@class/@id 嵌套链、
 *       ##正则替换、|| 或、!N 排除、@text/@title/@alt/@href/@src/@html/@content 提取、
 *       POST 搜索（method/body/charset）。
 * 不支持：@js:/<js> JavaScript、$.field/$[*] JSON 接口、验证码/登录。
 */
@Component
public class LegadoRuleConverter {

    private record Parsed(String selector, String type) {
    }

    public boolean isLegadoFormat(JsonNode node) {
        return node.has("bookSourceUrl")
                || node.has("ruleSearch")
                || node.has("ruleBookInfo")
                || node.has("ruleToc")
                || node.has("ruleContent");
    }

    public BookSourceRule convert(JsonNode n) {
        BookSourceRule rule = new BookSourceRule();
        rule.setName(text(n, "bookSourceName", "name"));
        rule.setUrl(text(n, "bookSourceUrl", "url"));
        rule.setCharset(text(n, "charset", "UTF-8"));
        parseSearchUrl(text(n, "searchUrl"), rule);
        rule.setBookUrlPattern(text(n, "bookUrlPattern"));

        JsonNode search = n.get("ruleSearch");
        if (search != null) {
            rule.setSearchList(sel(search, "bookList"));
            rule.setSearchName(sel(search, "name"));
            rule.setSearchNameType(type(search, "name"));
            rule.setSearchAuthor(sel(search, "author"));
            rule.setSearchAuthorType(type(search, "author"));
            rule.setSearchCover(sel(search, "coverUrl"));
            rule.setSearchDetail(sel(search, "bookUrl"));
        }

        JsonNode info = n.get("ruleBookInfo");
        if (info != null) {
            rule.setDetailName(sel(info, "name"));
            rule.setDetailNameType(type(info, "name"));
            rule.setDetailAuthor(sel(info, "author"));
            rule.setDetailAuthorType(type(info, "author"));
            rule.setDetailCover(sel(info, "coverUrl"));
            rule.setDetailIntro(sel(info, "intro"));
            rule.setDetailIntroType(type(info, "intro"));
        }

        JsonNode toc = n.get("ruleToc");
        if (toc != null) {
            rule.setCatalogList(sel(toc, "chapterList"));
            rule.setCatalogName(sel(toc, "chapterName"));
            rule.setCatalogUrl(sel(toc, "chapterUrl"));
        }

        JsonNode content = n.get("ruleContent");
        if (content != null) {
            rule.setBookContent(sel(content, "content"));
        }

        // legado 的目录通常在详情页上
        rule.setTocUrl("{{bookUrl}}");
        rule.setContentUrl("{{chapterUrl}}");
        return rule;
    }

    /**
     * 对已导入的规则（字段名已是内部格式，但选择器值可能仍是 legado DSL）重新解析转换。
     */
    public BookSourceRule reconvert(BookSourceRule rule) {
        String rawUrl = rule.getSearchUrl();
        rule.setSearchMethod(null);
        rule.setSearchBody(null);
        parseSearchUrl(rawUrl, rule);

        rule.setSearchList(parseSelector(rule.getSearchList()).selector());
        Parsed p = parseSelector(rule.getSearchName());
        rule.setSearchName(p.selector());
        rule.setSearchNameType(p.type());
        p = parseSelector(rule.getSearchAuthor());
        rule.setSearchAuthor(p.selector());
        rule.setSearchAuthorType(p.type());
        rule.setSearchCover(parseSelector(rule.getSearchCover()).selector());
        rule.setSearchDetail(parseSelector(rule.getSearchDetail()).selector());
        p = parseSelector(rule.getDetailName());
        rule.setDetailName(p.selector());
        rule.setDetailNameType(p.type());
        p = parseSelector(rule.getDetailAuthor());
        rule.setDetailAuthor(p.selector());
        rule.setDetailAuthorType(p.type());
        rule.setDetailCover(parseSelector(rule.getDetailCover()).selector());
        p = parseSelector(rule.getDetailIntro());
        rule.setDetailIntro(p.selector());
        rule.setDetailIntroType(p.type());
        rule.setCatalogList(parseSelector(rule.getCatalogList()).selector());
        rule.setCatalogName(parseSelector(rule.getCatalogName()).selector());
        rule.setCatalogUrl(parseSelector(rule.getCatalogUrl()).selector());
        rule.setBookContent(parseSelector(rule.getBookContent()).selector());
        return rule;
    }

    private String sel(JsonNode n, String key) {
        return parseSelector(text(n, key)).selector();
    }

    private String type(JsonNode n, String key) {
        return parseSelector(text(n, key)).type();
    }

    private String text(JsonNode n, String... keys) {
        for (String k : keys) {
            JsonNode v = n.get(k);
            if (v != null && !v.isNull() && !v.asText().isBlank()) {
                return v.asText();
            }
        }
        return null;
    }

    /**
     * 解析 legado 的 searchUrl，支持 "URL,{'method':'POST','body':'xxx','charset':'gbk'}" 形式。
     */
    private void parseSearchUrl(String searchUrl, BookSourceRule rule) {
        if (searchUrl == null || searchUrl.isBlank()) {
            return;
        }
        String s = searchUrl.trim();
        if (s.startsWith("@js:") || s.startsWith("<js")) {
            rule.setSearchUrl(null);
            return;
        }
        int idx = s.indexOf(",{");
        if (idx < 0) {
            rule.setSearchUrl(s);
            return;
        }
        rule.setSearchUrl(s.substring(0, idx).trim());
        String cfg = s.substring(idx + 1);
        Matcher mm = Pattern.compile("['\"]method['\"]\\s*:\\s*['\"]([^'\"]+)['\"]").matcher(cfg);
        if (mm.find()) {
            rule.setSearchMethod(mm.group(1).toUpperCase());
        }
        Matcher mb = Pattern.compile("['\"]body['\"]\\s*:\\s*['\"]([^'\"]*)['\"]").matcher(cfg);
        if (mb.find()) {
            rule.setSearchBody(mb.group(1));
        }
        Matcher mc = Pattern.compile("['\"]charset['\"]\\s*:\\s*['\"]([^'\"]+)['\"]").matcher(cfg);
        if (mc.find()) {
            rule.setCharset(mc.group(1));
        }
    }

    /**
     * 把 legado 选择器 DSL 转成 CSS / XPath 选择器，并识别提取方式。
     */
    private Parsed parseSelector(String s) {
        if (s == null || s.isBlank()) {
            return new Parsed(null, "text");
        }
        String sel = s.trim();

        // JS / JSON 规则不支持
        if (sel.contains("@js:") || sel.contains("<js")
                || sel.contains("$[") || sel.contains("$.") || sel.contains("@json:")) {
            return new Parsed(null, null);
        }

        // ## 正则替换
        int hash = sel.indexOf("##");
        if (hash >= 0) {
            sel = sel.substring(0, hash);
        }
        // || 或
        int or = sel.indexOf("||");
        if (or >= 0) {
            sel = sel.substring(0, or);
        }
        // !N 排除（连前面的 . 一起去掉，如 .!-1）
        sel = sel.replaceAll("\\.?!-?\\d+", "");

        // 提取方式后缀
        String type = "text";
        Matcher m = Pattern.compile("@(textNodes|text|href|src|title|alt|html|ownText|value|content|all)$").matcher(sel);
        if (m.find()) {
            type = m.group(1);
            sel = sel.substring(0, m.start());
        }

        // XPath
        if (sel.startsWith("@xpath:")) {
            Parsed p = parseXPath(sel.substring(7).trim());
            return new Parsed(p.selector(), p.type() != null ? p.type() : type);
        }
        if (sel.startsWith("//")) {
            Parsed p = parseXPath(sel);
            return new Parsed(p.selector(), p.type() != null ? p.type() : type);
        }

        // CSS
        if (sel.startsWith("@css:")) {
            sel = sel.substring(5);
        } else {
            sel = sel.replaceAll("@tag[.:]", " ").replaceAll("@class[.:]", ".").replaceAll("@id[.:]", "#");
            sel = sel.replaceAll("@([a-zA-Z][a-zA-Z0-9]*)", " $1");
            sel = sel.replaceAll("(^|[\\s(,])class\\.", "$1.")
                    .replaceAll("(^|[\\s(,])id\\.", "$1#")
                    .replaceAll("(^|[\\s(,])tag\\.", "$1");
        }

        // 索引后缀 .0 .1 .-1
        sel = sel.replaceAll("\\.(-?\\d+)(?![\\w-])", "");
        sel = sel.trim().replaceAll("\\s+", " ");
        return new Parsed(sel.isEmpty() ? null : sel, type);
    }

    /**
     * XPath 处理：去掉末尾多余斜杠；meta 标签转成 CSS + content 属性提取。
     */
    private Parsed parseXPath(String xpath) {
        String s = xpath.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1).trim();
        }
        Matcher m = Pattern.compile("^//meta\\[@([^=\\]]+)\\s*=\\s*['\"]([^'\"]+)['\"]\\]$").matcher(s);
        if (m.find()) {
            return new Parsed("meta[" + m.group(1).trim() + "='" + m.group(2) + "']", "content");
        }
        return new Parsed(s, null);
    }
}
