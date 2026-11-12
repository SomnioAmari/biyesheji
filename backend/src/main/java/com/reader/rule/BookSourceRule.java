package com.reader.rule;

import lombok.Data;

import java.util.Map;

/**
 * 书源规则（简化版书源模型）。
 * 选择器约定：以 "css:" 开头或默认走 CSS 选择器；以 "//" 或 ".//" 开头走 XPath。
 * 引擎对字段语义负责：name/author/intro 取文本，cover 取 src，detail/chapter url 取 href。
 */
@Data
public class BookSourceRule {
    private String name;
    private String url;
    private String charset = "UTF-8";
    private String userAgent;            // 自定义 User-Agent（反爬用，可选）
    private Map<String, String> headers; // 额外请求头（Referer/Cookie 等，反爬用，可选）

    // 搜索
    private String searchUrl;      // 模板，支持 {{key}}
    private String searchMethod;   // GET / POST，默认 GET
    private String searchBody;     // POST 请求体模板，支持 {{key}}
    private String searchList;     // 结果列表选择器
    private String searchName;
    private String searchAuthor;
    private String searchCover;
    private String searchDetail;   // 详情链接元素

    // 详情
    private String bookUrlPattern; // 用于从详情 URL 提取 bookId 的正则
    private String detailName;
    private String detailAuthor;
    private String detailCover;
    private String detailIntro;
    private String tocUrl;         // 目录 URL 模板，支持 {{bookId}}

    // 目录
    private String catalogList;
    private String catalogName;
    private String catalogUrl;

    // 正文
    private String contentUrl;     // 模板，支持 {{chapterUrl}}
    private String bookContent;    // 正文选择器

    // 文本字段的提取方式（legado 转换后使用）：null 或 text = 文本，title/alt/html/ownText/textNodes
    private String searchNameType;
    private String searchAuthorType;
    private String detailNameType;
    private String detailAuthorType;
    private String detailIntroType;
}
