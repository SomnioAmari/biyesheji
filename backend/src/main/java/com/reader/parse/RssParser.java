package com.reader.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class RssParser {

    public List<RssItem> parse(String xml) {
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        List<RssItem> items = new ArrayList<>();

        // RSS 2.0
        for (Element item : doc.select("item")) {
            items.add(new RssItem(
                    text(item, "title"),
                    text(item, "link"),
                    text(item, "author"),
                    parseDate(text(item, "pubDate")),
                    text(item, "description")
            ));
        }

        // Atom
        for (Element entry : doc.select("entry")) {
            String title = text(entry, "title");
            String link = "";
            Element linkEl = entry.selectFirst("link[href]");
            if (linkEl != null) {
                link = linkEl.attr("href");
            }
            String author = "";
            Element authorEl = entry.selectFirst("author > name");
            if (authorEl != null) {
                author = authorEl.text().trim();
            }
            String summary = text(entry, "summary");
            if (summary == null) {
                summary = text(entry, "content");
            }
            items.add(new RssItem(title, link, author, parseDate(text(entry, "updated")), summary));
        }
        return items;
    }

    private String text(Element el, String selector) {
        Element child = el.selectFirst(selector);
        if (child == null) {
            return null;
        }
        String t = child.text().trim();
        return t.isEmpty() ? null : t;
    }

    private LocalDateTime parseDate(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(s.trim()).toLocalDateTime();
        } catch (Exception ignore) {
        }
        try {
            return LocalDateTime.parse(s.trim(), DateTimeFormatter.RFC_1123_DATE_TIME);
        } catch (Exception ignore) {
        }
        try {
            return LocalDateTime.parse(s.trim().replace(' ', 'T'));
        } catch (Exception ignore) {
        }
        return null;
    }
}
