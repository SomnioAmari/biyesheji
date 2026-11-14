package com.reader.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按章节标题把整本书正文拆分成章节。
 * 识别「第X章/节/回/卷/部/篇/幕」「Chapter X」「序章/序幕/楔子/引子/前言/后记/尾声/番外/开篇」等标题，
 * 支持二字标题带空格（如「序 幕」「尾 声」「开 篇」），并支持把标题下一行的短副标题合并进章节名。
 * 拆分后自动过滤掉「空章节」（正文为空的目录条目），起到净化作用。
 */
public final class ChapterSplitter {

    public record Chapter(String name, String content) {
    }

    private static final Pattern MARKER = Pattern.compile(
            "(?:第\\s*[0-9零一二三四五六七八九十百千万两]+\\s*[章节回卷部篇集幕]"
                    + "|(?:Chapter|CHAPTER|chapter)\\s*[0-9]+"
                    + "|序\\s*章|序\\s*幕|序\\s*言|楔\\s*子|引\\s*子|前\\s*言|后\\s*记"
                    + "|尾\\s*声|番\\s*外|开\\s*篇|完结\\s*感言|卷末\\s*语)");

    /** 标题行长度上限，超过则视为正文（避免把正文里的「第X章」误判为标题）。 */
    private static final int MAX_HEADER_LINE_LENGTH = 60;

    private ChapterSplitter() {
    }

    public static List<Chapter> split(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        if (!normalized.isEmpty() && normalized.charAt(0) == '\uFEFF') {
            normalized = normalized.substring(1);
        }

        String[] lines = normalized.split("\n", -1);

        List<Chapter> chapters = new ArrayList<>();
        StringBuilder leading = new StringBuilder();
        String currentName = null;
        StringBuilder body = new StringBuilder();
        boolean anyHeader = false;

        for (int i = 0; i < lines.length; i++) {
            String raw = lines[i];
            String line = raw.trim();
            if (line.isEmpty()) {
                if (currentName != null) {
                    body.append('\n');
                } else if (!anyHeader && leading.length() > 0) {
                    leading.append('\n');
                }
                continue;
            }

            Matcher m = MARKER.matcher(line);
            if (line.length() <= MAX_HEADER_LINE_LENGTH && m.find()
                    && (m.end() >= line.length() || !isCjk(line.charAt(m.end())))) {
                if (currentName != null) {
                    addChapter(chapters, currentName, body);
                }
                String marker = normalizeMarker(m.group());
                String titleOnLine = line.substring(m.end()).trim();
                String name = marker;
                if (!titleOnLine.isEmpty()) {
                    name = marker + " " + titleOnLine;
                } else {
                    // 标题下一行若是短副标题，则合并进章节名
                    int j = i + 1;
                    while (j < lines.length && lines[j].trim().isEmpty()) {
                        j++;
                    }
                    if (j < lines.length) {
                        String next = lines[j].trim();
                        if (next.length() < 30 && !MARKER.matcher(next).find()) {
                            name = marker + " " + next;
                            i = j; // 跳过副标题行
                        }
                    }
                }
                currentName = truncate(name);
                body = new StringBuilder();
                anyHeader = true;
            } else if (currentName != null) {
                if (body.length() > 0) {
                    body.append('\n');
                }
                body.append(raw);
            } else {
                if (leading.length() > 0) {
                    leading.append('\n');
                }
                leading.append(raw);
            }
        }

        if (!anyHeader) {
            String whole = clean(text.trim());
            return whole.isEmpty() ? List.of() : List.of(new Chapter("正文", whole));
        }

        if (currentName != null) {
            addChapter(chapters, currentName, body);
        }
        String lead = clean(leading);
        if (!lead.isEmpty()) {
            chapters.add(0, new Chapter("前言", lead));
        }
        return chapters;
    }

    /** 只在正文非空时加入章节，过滤掉目录条目产生的空章节。 */
    private static void addChapter(List<Chapter> chapters, String name, StringBuilder body) {
        String content = clean(body);
        if (!content.isEmpty()) {
            chapters.add(new Chapter(name, content));
        }
    }

    /** 判断是否为汉字，用于排除「前言后语」这类把「前言」当标题的误判。 */
    private static boolean isCjk(char c) {
        return Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN;
    }

    /** 规范化标题标记：中文二字标题去掉字间空格（「序 幕」→「序幕」），Chapter 保持原样。 */
    private static String normalizeMarker(String marker) {
        String m = marker.trim();
        if (m.toLowerCase().startsWith("chapter")) {
            return m;
        }
        return m.replaceAll("\\s+", "");
    }

    private static String clean(StringBuilder body) {
        return body.toString().trim();
    }

    private static String clean(String s) {
        return s == null ? "" : s.trim();
    }

    private static String truncate(String s) {
        return s.length() > 200 ? s.substring(0, 200) : s;
    }
}
