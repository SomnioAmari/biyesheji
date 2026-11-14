# 纯净阅读 —— 可配置内容源聚合与纯净阅读系统

基于 Spring Boot + MyBatis + MySQL + Redis 与 Vue 3 + Element Plus 的前后端分离网站。

新闻与书籍两个独立板块：

- **新闻板块**：用户配置 RSS / 网页源，系统定时批量抓取、正文清洗后写入 MySQL 并缓存到 Redis，主页按「源权重 + 新鲜度 + 阅读行为」个性化排序展示。
- **书籍板块**：按 legado 思路做网页复现，导入书源规则后**只搜索、不推荐**，按书名搜索 → 加入书架 → 目录 → 章节阅读。

纯净阅读由**后端正文清洗**保证（正文密度打分算法 + 白名单标签），前端只做展示。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.5、MyBatis、MySQL 8、Redis、JWT(jjwt)、Jsoup、Lombok |
| 前端 | Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios |
| 部署 | Nginx（静态托管 + 反向代理） |

## 目录结构

```
毕业设计/
├── backend/                 # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/reader/
│       ├── common/          # 统一返回、异常、JWT、拦截器
│       ├── config/          # Web/CORS 配置
│       ├── controller/      # REST 接口
│       ├── service/         # 业务逻辑
│       ├── mapper/          # MyBatis Mapper
│       ├── entity/ dto/ vo/ # 实体/传输/视图对象
│       ├── parse/           # HTTP 抓取、RSS 解析、正文清洗
│       ├── rule/            # 书源规则引擎
│       └── scheduler/       # 定时抓取
├── frontend/                # Vue 3 前端
├── sql/schema.sql           # 数据库建表脚本
├── docs/                    # 书源规则示例、设计文档
└── 需求与总体方案设计.md
```

## 环境要求

- JDK 17+（本项目用 JDK 24 验证，编译目标 Java 21）
- Maven 3.9+
- Node.js 18+（用 Node 24 验证）、npm（pnpm 也可，但新版 pnpm 的供应链策略可能拦截刚发布的依赖，建议用 npm）
- MySQL 8.x
- Redis（可选，未启动时新闻主页自动降级为直接查库）

## 快速启动

### 1. 初始化数据库

```bash
mysql -uroot -p < sql/schema.sql
```

### 2. 启动后端

```bash
cd backend
# 默认连接 localhost:3306/reader，用户 root，密码 root（用环境变量覆盖）
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的密码"
mvn spring-boot:run
```

后端默认端口 8080。可用环境变量：`DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`、`JWT_SECRET`。

### 3. 启动前端（开发模式）

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173 ，开发服务器已把 `/api` 代理到 http://localhost:8080。

## 生产部署（Nginx）

```bash
# 前端打包
cd frontend && npm run build          # 产物在 frontend/dist

# 后端打包
cd backend && mvn -DskipTests package   # 产物 target/pure-reader-backend-1.0.0.jar
```

Nginx 示例配置：

```nginx
server {
    listen 80;
    server_name localhost;

    # 前端静态资源
    root /path/to/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端接口反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 书源规则说明

书源规则是一个 JSON，字段语义（见 `docs/书源规则示例.json`）：

| 字段 | 含义 |
| --- | --- |
| `searchUrl` | 搜索 URL 模板，`{{key}}` 为关键词占位 |
| `searchList` | 搜索结果列表选择器 |
| `searchName` / `searchAuthor` | 书名 / 作者（取文本） |
| `searchCover` / `searchDetail` | 封面（取 src）/ 详情链接（取 href） |
| `bookUrlPattern` | 从详情 URL 提取 bookId 的正则（第一个分组） |
| `detailName` / `detailAuthor` / `detailIntro` | 详情页字段 |
| `tocUrl` | 目录 URL 模板，`{{bookId}}` 占位 |
| `catalogList` / `catalogName` / `catalogUrl` | 目录列表 / 章节名 / 章节链接 |
| `contentUrl` | 正文 URL 模板，`{{chapterUrl}}` 占位 |
| `bookContent` | 正文选择器 |

选择器约定：以 `//` 或 `.//` 开头为 XPath，其余（或 `css:` 前缀）为 CSS 选择器。

### 导入 legado（开源阅读）书源

系统支持直接导入 legado 书源 JSON，会自动转换 `ruleSearch` / `ruleBookInfo` / `ruleToc` / `ruleContent` 结构，以及 `class.` / `id.` / `tag.` / `@css:` / `@xpath:` 等选择器语法：

- **粘贴导入**：书源管理页粘贴 legado 书源 JSON（支持单个或合集数组）
- **URL 导入**：输入指向书源 JSON 的链接，系统自动抓取并导入

> 支持常见 legado 规则；少数使用 `@json:`、正则、JS、`&&` 组合等高级语法的书源可能无法完整转换，可导入后手动编辑补全。

## 个性化排序

```
score = α·源权重 + β·新鲜度(时间衰减) + γ·行为分(点击/读完)
```

系数与时间半衰期可在 `backend/src/main/resources/application.yml` 的 `app.rank` 中调整。

## 主要接口

| 模块 | 路径 |
| --- | --- |
| 认证 | `POST /api/auth/register`、`POST /api/auth/login` |
| 新闻源 | `GET/POST/PUT/DELETE /api/news-sources`、`POST /api/news-sources/{id}/fetch` |
| 资讯 | `GET /api/articles`、`GET /api/articles/{id}`、`POST/DELETE /api/articles/{id}/favorite` |
| 书源 | `GET/POST/PUT/DELETE /api/book-sources`、`POST /api/book-sources/import`（粘贴导入）、`POST /api/book-sources/import-url`（URL 导入） |
| 书籍 | `GET /api/novel/search`、`/detail`、`/{bookId}`、`/{bookId}/catalog`、`/{bookId}/{chapterId}` |
| 书架 | `GET/POST/DELETE /api/bookshelf` |
| 进度 | `GET/PUT /api/progress` |
