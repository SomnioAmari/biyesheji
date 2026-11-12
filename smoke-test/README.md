# 本地冒烟测试数据

本目录包含一套**本地测试内容源**，用于不依赖外网的情况下，端到端验证系统的新闻与小说两条抓取解析链路。

## 内容

- **新闻源**：`feed.xml`（RSS）+ `article1~3.html`（含广告/导航/页脚的测试文章）
- **小说书源**：`search.html` + `book1.html` + `catalog1.html` + `chapter1~3.html`（模拟小说站）
- **验证脚本**：`check.py`（正文清洗）、`novel_check.py`（小说链路）、`chinese_check.py`（中文搜索）

## 使用方法

1. 启动本地静态服务器（端口 8090）：

```bash
python -m http.server 8090
```

2. 启动后端后，添加内容源：

- 新闻源：URL 填 `http://localhost:8090/feed.xml`（类型 RSS）
- 书源：书源首页 `http://localhost:8090`，规则见 `novel_check.py` 中的 `rule` 字典

3. 运行验证脚本：

```bash
python check.py          # 验证新闻正文清洗
python novel_check.py    # 验证小说搜索/详情/目录/章节/书架
python chinese_check.py  # 验证中文关键词搜索
```

> 说明：这些本地文件模拟了带广告和导航的真实网页，用来演示"后端正文清洗"能力，也方便答辩时离线演示全流程。
