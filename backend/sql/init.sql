CREATE DATABASE IF NOT EXISTS campus_zhihu DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_zhihu;

SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名/学号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-学生, 1-教师, 2-管理员',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
  `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
  `department` VARCHAR(100) DEFAULT NULL COMMENT '院系',
  `question_count` INT NOT NULL DEFAULT 0 COMMENT '提问数',
  `answer_count` INT NOT NULL DEFAULT 0 COMMENT '回答数',
  `follower_count` INT NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `following_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '获赞数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 标签表
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '标签描述',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '标签图标',
  `question_count` INT NOT NULL DEFAULT 0 COMMENT '问题数量',
  `follower_count` INT NOT NULL DEFAULT 0 COMMENT '关注人数',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ----------------------------
-- 问题表
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容（支持富文本）',
  `user_id` BIGINT NOT NULL COMMENT '提问者ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-草稿, 1-已发布, 2-已关闭, 3-待审核, 4-已拒绝',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  `answer_count` INT NOT NULL DEFAULT 0 COMMENT '回答数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶: 0-否, 1-是',
  `accepted_answer_id` BIGINT DEFAULT NULL COMMENT '被采纳回答ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_like_count` (`like_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题表';

-- ----------------------------
-- 问题-标签关联表
-- ----------------------------
DROP TABLE IF EXISTS `question_tag`;
CREATE TABLE `question_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL COMMENT '问题ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_tag` (`question_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题标签关联表';

-- ----------------------------
-- 回答表
-- ----------------------------
DROP TABLE IF EXISTS `answer`;
CREATE TABLE `answer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回答ID',
  `content` TEXT NOT NULL COMMENT '回答内容',
  `question_id` BIGINT NOT NULL COMMENT '所属问题ID',
  `user_id` BIGINT NOT NULL COMMENT '回答者ID',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `is_accepted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否被采纳: 0-否, 1-是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-待审核, 1-已发布, 2-已拒绝',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_question_id` (`question_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_like_count` (`like_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回答表';

-- ----------------------------
-- 评论表（通用，支持对问题和回答的评论）
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
  `user_id` BIGINT NOT NULL COMMENT '评论者ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型: 1-问题, 2-回答',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（用于回复）',
  `reply_user_id` BIGINT DEFAULT NULL COMMENT '被回复用户ID',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-待审核, 1-已发布, 2-已拒绝',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ----------------------------
-- 点赞表
-- ----------------------------
DROP TABLE IF EXISTS `user_like`;
CREATE TABLE `user_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型: 1-问题, 2-回答, 3-评论',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- ----------------------------
-- 收藏表
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `question_id` BIGINT NOT NULL COMMENT '问题ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ----------------------------
-- 关注表
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '关注者ID',
  `follow_user_id` BIGINT NOT NULL COMMENT '被关注者ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
  KEY `idx_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注表';

-- ----------------------------
-- 通知表
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收者ID',
  `sender_id` BIGINT DEFAULT NULL COMMENT '发送者ID',
  `type` TINYINT NOT NULL COMMENT '类型: 1-回答, 2-评论, 3-点赞, 4-关注, 5-采纳, 6-系统',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '通知标题',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
  `target_type` TINYINT DEFAULT NULL COMMENT '关联目标类型: 1-问题, 2-回答, 3-评论, 4-用户',
  `target_id` BIGINT DEFAULT NULL COMMENT '关联目标ID',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ----------------------------
-- 举报表
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `user_id` BIGINT NOT NULL COMMENT '举报者ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型: 1-问题, 2-回答, 3-评论, 4-用户',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `reason` TINYINT NOT NULL COMMENT '举报原因: 1-垃圾广告, 2-违规内容, 3-侮辱谩骂, 4-抄袭, 5-其他',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '补充说明',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态: 0-待处理, 1-已处理, 2-已驳回',
  `handle_note` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
  `handle_user_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- ----------------------------
-- 草稿表
-- ----------------------------
DROP TABLE IF EXISTS `question_draft`;
CREATE TABLE `question_draft` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '草稿ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '标题',
  `content` TEXT DEFAULT NULL COMMENT '内容',
  `tag_ids` VARCHAR(255) DEFAULT NULL COMMENT '标签ID（逗号分隔）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题草稿表';
