package com.reader.parse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlCleanerTest {

    private final HtmlCleaner cleaner = new HtmlCleaner();

    @Test
    void shouldExtractArticleBodyAndRemoveAds() {
        String html = "<html><body>"
                + "<nav>首页 导航 栏目</nav>"
                + "<div class='ad'>广告内容广告内容广告内容广告内容广告内容</div>"
                + "<div id='content'>"
                + "<p>这是正文第一段。系统需要根据文本长度与标点密度正确识别出正文区域。</p>"
                + "<p>这是正文第二段。页面上的干扰元素应当被过滤，只保留干净的正文内容。</p>"
                + "<p>这是正文第三段。正文密度打分算法会奖励包含多个段落的容器节点。</p>"
                + "</div>"
                + "<footer>版权所有</footer>"
                + "</body></html>";

        String content = cleaner.extractContent(html, "https://example.com/a/1");

        assertThat(content).contains("正文第一段");
        assertThat(content).contains("正文第二段");
        assertThat(content).contains("正文第三段");
        assertThat(content).doesNotContain("广告内容");
        assertThat(content).doesNotContain("首页");
        assertThat(content).doesNotContain("版权所有");
    }

    @Test
    void shouldAbsoluteImageUrl() {
        String html = "<div id='content'>"
                + "<p>这是一段足够长的正文内容，用来触发正文密度算法的识别，确保容器被选中。</p>"
                + "<p>第二段正文，包含图片。</p>"
                + "<img src='/img/pic.jpg'>"
                + "</div>";

        String content = cleaner.extractContent(html, "https://example.com/a/1");

        assertThat(content).contains("https://example.com/img/pic.jpg");
    }

    @Test
    void summaryShouldTruncateLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("这是一段摘要测试文本。");
        }
        String summary = cleaner.summary(sb.toString());
        assertThat(summary).hasSize(200);
    }
}
