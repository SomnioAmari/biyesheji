package com.reader.parse;

import java.time.LocalDateTime;

public record RssItem(String title,
                      String link,
                      String author,
                      LocalDateTime pubDate,
                      String description) {
}
