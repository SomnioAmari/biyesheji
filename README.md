# 纯净阅读 —— 可配置内容源聚合与纯净阅读系统

基于 Spring Boot + MyBatis + MySQL + Redis 与 Vue 3 + Element Plus 的前后端分离网站。

系统分**新闻**与**书籍**两个独立板块，并带**用户 / 管理员双角色**：

- **新闻板块**：用户配置 RSS / 网页源，系统定时抓取、正文清洗后入库，主页按「源权重 + 新鲜度 + 阅读行为」个性化排序展示，支持收藏、阅读进度、朗读。
- **书籍板块**：用户上传 TXT（自动拆章）经管理员审核入库，或导入书源规则按书名搜索；支持书架、阅读进度、可调排版、长章节分页、朗读。

纯净阅读由**后端正文清洗**保证（正文密度打分 + 白名单标签），前端只做展示。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.5、MyBatis、MySQL 8、Redis、JWT(jjwt)、Jsoup、Lombok |
| 前端 | Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios、Web Speech API(朗读) |
| 部署 | Nginx（静态托管 + 反向代理） |

## 功能特性

- **双角色**：普通用户 / 管理员；管理员负责用户管理、权限分配、书籍审核。
- **书籍上传审核**：用户上传 TXT（自动识别章节标题拆分、支持 UTF-8 / GBK / UTF-16），管理员审核通过后进入公共书库。
- **内部书库优先搜索**：先搜本地已审核书籍，搜不到或不是想要的，可再按自定义书源搜索。
- **书源规则引擎**：legado 思路，规则 JSON 描述「搜索/详情/目录/正文」，支持章节分页拼接、legado 书源导入。
- **纯净阅读**：正文清洗由后端完成；阅读页支持字号、行距、段距、字距、字体、背景主题、页宽可调，长章节自动分页。
- **朗读（TTS）**：浏览器语音朗读新闻与书籍，支持语速/音量/音色、段落高亮、底部播放条。
- **收藏与进度**：新闻收藏、书籍书架、阅读进度；书架里「收藏的小说 / 收藏的新闻」分开管理。

## 目录结构

```
毕业设计/
├── backend/                # Spring Boot 后端
│   └── src/main/java/com/reader/
│       ├── common/         # 统一返回、异常、JWT、拦截器
│       ├── config/         # Web/CORS/上传目录、管理员初始化
│       ├── controller/     # REST 接口
│       ├── service/        # 业务逻辑（认证/新闻/书籍/上传审核/文件）
│       ├── mapper/         # MyBatis Mapper
│       ├── entity/ dto/ vo/
│       ├── parse/          # HTTP 抓取、RSS 解析、正文清洗、章节拆分
│       └── rule/           # 书源规则引擎
├── frontend/               # Vue 3 前端
│   └── src/
│       ├── api/            # 接口封装
│       ├── composables/    # useTts 等
│       ├── components/     # TtsBar 等
│       ├── store/ router/
│       └── views/          # 各页面
├── sql/
│   ├── schema.sql          # 建表脚本（全新安装）
│   └── upgrade_v2.sql      # v1 → v2 升级脚本
├── docs/                   # 设计文档、书源示例
└── 需求与总体方案设计.md
```

## 环境要求

- JDK 17+（用 JDK 24 验证，编译目标 Java 21）
- Maven 3.9+
- Node.js 18+、npm
- MySQL 8.x
- Redis（可选，未启动时新闻主页自动降级为直接查库）

## 快速启动

### 1. 初始化数据库

全新安装：

```bash
mysql -uroot -p < sql/schema.sql
```

已有旧版库：执行 `sql/upgrade_v2.sql`。

### 2. 启动后端

```bash
cd backend
# 默认连接 localhost:3306/reader，用户 root，密码 10132（用环境变量覆盖）
mvn spring-boot:run
```

后端默认端口 8080。可用环境变量：`DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`、`JWT_SECRET`。

### 3. 启动前端（开发模式）

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173 ，开发服务器把 `/api` 代理到 http://localhost:8080。

### 默认账号

- 管理员：`admin / admin123`（启动时自动创建）
- 普通用户：注册新账号即可

## 生产部署（Nginx）

```bash
cd frontend && npm run build          # 产物在 frontend/dist
cd backend && mvn -DskipTests package  # 产物 target/pure-reader-backend-1.0.0.jar
```

Nginx 示例：前端静态资源 `root frontend/dist`，`/api/` 反向代理到 `127.0.0.1:8080`，并把 `/uploads/` 也代理到后端（封面图片本地存储）。

## 更多文档

- 需求与总体方案设计：`需求与总体方案设计.md`
- 真实内容源清单：`docs/真实内容源清单.md`
