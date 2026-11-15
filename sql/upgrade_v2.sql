-- =============================================================
-- 已有数据库升级脚本（v1 → v2）
-- 仅需执行一次；若字段已存在请忽略对应报错。
-- 数据库名：reader
-- =============================================================
USE `reader`;

-- 1) user 表：新增角色与状态
ALTER TABLE `user`
  ADD COLUMN `role`   TINYINT NOT NULL DEFAULT 0 COMMENT '0=普通用户, 1=管理员' AFTER `email`,
  ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常, 0=禁用' AFTER `role`;

-- 2) novel_book 表：source_id 允许为空；新增上传与审核字段
ALTER TABLE `novel_book`
  MODIFY COLUMN `source_id` BIGINT DEFAULT NULL COMMENT '所属书源(书源书籍)；上传书籍为空',
  ADD COLUMN `uploader_id`   BIGINT       DEFAULT NULL COMMENT '上传者(上传书籍)' AFTER `source_id`,
  ADD COLUMN `category`      VARCHAR(50)  DEFAULT NULL COMMENT '分类' AFTER `intro`,
  ADD COLUMN `review_status` TINYINT      NOT NULL DEFAULT 1 COMMENT '0=待审核, 1=已通过, 2=已拒绝' AFTER `status`,
  ADD COLUMN `review_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因' AFTER `review_status`,
  ADD COLUMN `reviewer_id`   BIGINT       DEFAULT NULL COMMENT '审核人' AFTER `review_reason`,
  ADD COLUMN `reviewed_at`   DATETIME     DEFAULT NULL COMMENT '审核时间' AFTER `reviewer_id`,
  ADD KEY `idx_uploader` (`uploader_id`),
  ADD KEY `idx_review` (`review_status`);

-- 3) novel_chapter 表：url 允许为空（上传书籍的章节无外部链接）
ALTER TABLE `novel_chapter`
  MODIFY COLUMN `url` VARCHAR(500) DEFAULT NULL COMMENT '章节链接（上传书籍为空）';

-- 4) article 表：去重键改为 (source_id, url_hash)，修复多用户订阅同一源时互相去重导致入库 0 条
ALTER TABLE `article`
  DROP INDEX `uk_url_hash`,
  ADD UNIQUE KEY `uk_source_url` (`source_id`, `url_hash`);
