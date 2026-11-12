package com.reader.rule;

import lombok.Data;

/**
 * 网页新闻源抽取规则（type=1 的新闻源使用）。
 */
@Data
public class WebSourceRule {
    private String listSelector;    // 列表页文章链接选择器
    private String titleSelector;   // 详情页标题选择器
    private String authorSelector;  // 详情页作者选择器
    private String contentSelector; // 详情页正文选择器（可选，为空走密度算法）
}
