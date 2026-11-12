package com.reader.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class HtmlCleaner {

    private static final Safelist CONTENT_SAFELIST = Safelist.none()
            .addTags("p", "div", "span", "h1", "h2", "h3", "h4", "h5", "h6",
                    "br", "hr", "img", "a", "blockquote", "pre", "code",
                    "ul", "ol", "li", "strong", "em", "b", "i", "u",
                    "table", "thead", "tbody", "tr", "td", "th", "figure", "figcaption")
            .addAttributes("a", "href", "title")
            .addAttributes("img", "src", "alt", "title")
            .addProtocols("a", "href", "http", "https")
            .addProtocols("img", "src", "http", "https");

    /**
     * 基于正文密度打分算法抽取正文，返回干净 HTML。
     * 参考 Readability / Boilerpipe 思路：
     * 文本长度 + 标点密度加分，链接密度扣分；同时给"包含多个段落"的容器加分，
     * 从而抽出整块正文容器而非单个段落。
     */
    public String extractContent(String html, String baseUri) {
        Document doc = Jsoup.parse(html, baseUri);
        doc.select("script,style,noscript,iframe,nav,header,footer,aside,form,button,input,select").remove();
        doc.select("[class*=ad],[id*=ad],[class*=recommend],[id*=recommend],[class*=related]," +
                "[class*=comment],[class*=share],[class*=copyright],[class*=footer],[class*=sidebar]").remove();

        Element best = null;
        double bestScore = 0;
        for (Element el : doc.select("p, div, article, section, td, blockquote")) {
            double score = scoreBlock(el);
            if (score > bestScore) {
                bestScore = score;
                best = el;
            }
        }
        if (best == null) {
            return "";
        }

        for (Element img : best.select("img")) {
            String src = img.absUrl("src");
            if (!src.isBlank()) {
                img.attr("src", src);
            }
        }
        for (Element a : best.select("a")) {
            String href = a.absUrl("href");
            if (!href.isBlank()) {
                a.attr("href", href);
            }
        }
        return Jsoup.clean(best.html(), baseUri, CONTENT_SAFELIST);
    }

    private double scoreBlock(Element el) {
        String ownText = el.ownText();
        int ownLen = ownText.length();
        int punct = countPunct(ownText);

        // 容器内所有 <p> 的文本长度，奖励"正文容器"
        int descPLen = 0;
        for (Element p : el.select("p")) {
            if (p == el) {
                continue; // Jsoup 的 select 会包含元素自身，跳过避免重复计分
            }
            descPLen += p.ownText().length();
        }

        int linkLen = 0;
        for (Element a : el.select("a")) {
            linkLen += a.text().length();
        }
        int totalText = el.text().length();
        double linkDensity = linkLen / (double) Math.max(1, totalText);

        double score = ownLen + punct * 2.0 + descPLen * 1.5;
        if (linkDensity > 0.3) {
            score *= 0.3;
        }
        return score;
    }

    private int countPunct(String text) {
        int punct = 0;
        for (char c : text.toCharArray()) {
            if (c == ',' || c == '，' || c == '.' || c == '。' || c == '；'
                    || c == ';' || c == '：' || c == ':' || c == '！' || c == '!') {
                punct++;
            }
        }
        return punct;
    }

    public String summary(String html) {
        String text = Jsoup.parse(html).text().trim();
        return text.length() > 200 ? text.substring(0, 200) : text;
    }
}
