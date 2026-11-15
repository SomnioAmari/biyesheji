-- =============================================================
-- 基于 Spring Boot 的可配置内容源聚合与纯净阅读系统
-- MySQL 8.x 建表脚本
-- 数据库名：reader  字符集：utf8mb4
-- =============================================================

CREATE DATABASE IF NOT EXISTS `reader`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `reader`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`   VARCHAR(50)  NOT NULL COMMENT '用户名',
  `password`   VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
  `nickname`   VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `email`      VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0=普通用户, 1=管理员',
  `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '1=正常, 0=禁用',
  `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 新闻源表（RSS / 网页源）
CREATE TABLE IF NOT EXISTS `news_source` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`        BIGINT       NOT NULL COMMENT '归属用户',
  `type`           TINYINT      NOT NULL DEFAULT 0 COMMENT '0=RSS, 1=网页源',
  `name`           VARCHAR(100) NOT NULL COMMENT '源名称',
  `url`            VARCHAR(500) NOT NULL COMMENT 'RSS/列表页地址',
  `category`       VARCHAR(50)  DEFAULT NULL COMMENT '分类/标签',
  `rule_json`      TEXT         COMMENT '网页源抽取规则(列表/详情选择器)',
  `weight`         INT          NOT NULL DEFAULT 5 COMMENT '源权重(个性化排序)',
  `fetch_interval` INT          NOT NULL DEFAULT 60 COMMENT '抓取间隔(分钟)',
  `enabled`        TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用',
  `last_fetch_at`  DATETIME     DEFAULT NULL,
  `created_at`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='新闻源表';

-- 新闻文章表
CREATE TABLE IF NOT EXISTS `article` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `source_id`    BIGINT       NOT NULL COMMENT '所属新闻源',
  `title`        VARCHAR(255) NOT NULL,
  `author`       VARCHAR(100) DEFAULT NULL,
  `source_name`  VARCHAR(100) DEFAULT NULL COMMENT '来源标注',
  `url`          VARCHAR(500) NOT NULL COMMENT '原始链接',
  `url_hash`     CHAR(64)     NOT NULL COMMENT '去重键(SHA-256)',
  `summary`      TEXT         COMMENT '摘要',
  `content`      LONGTEXT     COMMENT '清洗后正文',
  `cover`        VARCHAR(255) DEFAULT NULL,
  `published_at` DATETIME     DEFAULT NULL COMMENT '发布时间(排序用)',
  `fetched_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `created_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source_url` (`source_id`, `url_hash`),
  KEY `idx_source` (`source_id`),
  KEY `idx_published` (`published_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='新闻文章表';

-- 书源表
CREATE TABLE IF NOT EXISTS `book_source` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT       NOT NULL,
  `name`       VARCHAR(100) NOT NULL COMMENT '书源名称',
  `url`        VARCHAR(500) NOT NULL COMMENT '书源首页',
  `rule_json`  TEXT         NOT NULL COMMENT '完整书源规则',
  `enabled`    TINYINT      NOT NULL DEFAULT 1,
  `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='书源表';

-- 书籍表
CREATE TABLE IF NOT EXISTS `novel_book` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `source_id`     BIGINT       DEFAULT NULL COMMENT '所属书源(书源书籍)；上传书籍为空',
  `uploader_id`   BIGINT       DEFAULT NULL COMMENT '上传者(上传书籍)',
  `name`          VARCHAR(200) NOT NULL,
  `author`        VARCHAR(100) DEFAULT NULL,
  `cover`         VARCHAR(255) DEFAULT NULL,
  `intro`         TEXT,
  `category`      VARCHAR(50)  DEFAULT NULL COMMENT '分类',
  `url`           VARCHAR(500) DEFAULT NULL COMMENT '详情页地址',
  `toc_url`       VARCHAR(500) DEFAULT NULL COMMENT '目录页地址(规则引擎解析得到)',
  `status`        VARCHAR(20)  DEFAULT NULL COMMENT '连载/完结',
  `review_status` TINYINT      NOT NULL DEFAULT 1 COMMENT '0=待审核, 1=已通过, 2=已拒绝',
  `review_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
  `reviewer_id`   BIGINT       DEFAULT NULL COMMENT '审核人',
  `reviewed_at`   DATETIME     DEFAULT NULL COMMENT '审核时间',
  `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_source` (`source_id`),
  KEY `idx_uploader` (`uploader_id`),
  KEY `idx_review` (`review_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='书籍表';

-- 章节表（按需抓取后缓存正文）
CREATE TABLE IF NOT EXISTS `novel_chapter` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `book_id`       BIGINT       NOT NULL,
  `name`          VARCHAR(255) NOT NULL COMMENT '章节名',
  `url`           VARCHAR(500) DEFAULT NULL COMMENT '章节链接（上传书籍为空）',
  `content`       LONGTEXT     COMMENT '清洗后正文',
  `chapter_index` INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
  `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_book` (`book_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='章节表';

-- 书架表（书籍专属收藏）
CREATE TABLE IF NOT EXISTS `bookshelf` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT   NOT NULL,
  `book_id`    BIGINT   NOT NULL,
  `added_at`   DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_book` (`user_id`, `book_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='书架表';

-- 新闻收藏表
CREATE TABLE IF NOT EXISTS `article_favorite` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT   NOT NULL,
  `article_id` BIGINT   NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='新闻收藏表';

-- 阅读进度表（通用：0=文章, 1=书籍）
CREATE TABLE IF NOT EXISTS `reading_progress` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT        NOT NULL,
  `target_type`     TINYINT       NOT NULL COMMENT '0=文章, 1=书籍',
  `target_id`       BIGINT        NOT NULL,
  `chapter_id`      BIGINT        DEFAULT NULL COMMENT '章节(文章为空)',
  `scroll_position` DECIMAL(10,4) DEFAULT NULL COMMENT '文章滚动百分比(0~1)',
  `last_read_at`    DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='阅读进度表';

-- 用户行为表（供新闻个性化排序）
CREATE TABLE IF NOT EXISTS `user_behavior` (
  `id`             BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`        BIGINT   NOT NULL,
  `target_type`    TINYINT  NOT NULL COMMENT '0=文章, 1=书籍',
  `target_id`      BIGINT   NOT NULL,
  `click_count`    INT      NOT NULL DEFAULT 0,
  `read_duration`  INT      NOT NULL DEFAULT 0 COMMENT '阅读时长(秒)',
  `read_complete`  TINYINT  NOT NULL DEFAULT 0 COMMENT '是否读完',
  `last_action_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户行为表';

-- 抓取日志表
CREATE TABLE IF NOT EXISTS `fetch_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `source_type`   TINYINT      NOT NULL COMMENT '0=新闻源, 1=书源',
  `source_id`     BIGINT       NOT NULL,
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1=成功, 0=失败',
  `message`       VARCHAR(500) DEFAULT NULL,
  `fetched_count` INT          NOT NULL DEFAULT 0 COMMENT '入库条数',
  `started_at`    DATETIME     DEFAULT NULL,
  `finished_at`   DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_source` (`source_type`, `source_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='抓取日志表';
