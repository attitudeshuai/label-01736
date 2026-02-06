-- 研究生培养科研管理系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS research_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE research_db;

-- 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` TINYINT NOT NULL DEFAULT 3 COMMENT '角色:1-管理员,2-导师,3-学生',
    `supervisor_id` BIGINT DEFAULT NULL COMMENT '导师ID(学生专用)',
    `student_id` VARCHAR(30) DEFAULT NULL COMMENT '学号',
    `research_direction` VARCHAR(200) DEFAULT NULL COMMENT '研究方向',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_supervisor_id` (`supervisor_id`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 论文阅读记录表
DROP TABLE IF EXISTS `paper_reading`;
CREATE TABLE `paper_reading` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `reading_date` DATE NOT NULL COMMENT '阅读日期',
    `title` VARCHAR(500) NOT NULL COMMENT '论文题目',
    `keywords` VARCHAR(300) DEFAULT NULL COMMENT '关键字',
    `source` VARCHAR(200) DEFAULT NULL COMMENT '来源(期刊/会议)',
    `authors` VARCHAR(500) DEFAULT NULL COMMENT '作者',
    `first_institution` VARCHAR(200) DEFAULT NULL COMMENT '第一单位',
    `work_introduction` TEXT COMMENT '论文工作介绍',
    `innovation_points` TEXT COMMENT '最大创新点或贡献',
    `thoughts_or_drawbacks` TEXT COMMENT '思考或缺点',
    `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_reading_date` (`reading_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论文阅读记录表';

-- 成果登记表
DROP TABLE IF EXISTS `achievement`;
CREATE TABLE `achievement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` TINYINT NOT NULL COMMENT '类型:1-论文,2-专利,3-软著',
    `title` VARCHAR(500) NOT NULL COMMENT '成果名称',
    `authors` VARCHAR(500) DEFAULT NULL COMMENT '作者/发明人',
    `publication_venue` VARCHAR(300) DEFAULT NULL COMMENT '发表刊物/授权单位',
    `publication_date` DATE DEFAULT NULL COMMENT '发表/授权日期',
    `doi_or_number` VARCHAR(100) DEFAULT NULL COMMENT 'DOI/专利号/登记号',
    `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
    `description` TEXT COMMENT '描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-已发表/授权,2-申请中',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成果登记表';

-- 周报表
DROP TABLE IF EXISTS `weekly_report`;
CREATE TABLE `weekly_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `year` INT NOT NULL COMMENT '年份',
    `week_number` INT NOT NULL COMMENT '周数',
    `week_start` DATE NOT NULL COMMENT '周开始日期',
    `week_end` DATE NOT NULL COMMENT '周结束日期',
    `work_content` TEXT COMMENT '本周工作内容(JSON格式)',
    `current_progress` TEXT COMMENT '当前进展情况',
    `next_week_plan` TEXT COMMENT '下周计划安排',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-草稿,2-已提交,3-已审阅',
    `supervisor_comment` TEXT COMMENT '导师评语',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_year_week` (`user_id`, `year`, `week_number`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_year_week` (`year`, `week_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周报表';

-- 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `operation` VARCHAR(100) DEFAULT NULL COMMENT '操作描述',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    `params` TEXT COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `status` TINYINT DEFAULT 1 COMMENT '状态:0-失败,1-成功',
    `error_msg` TEXT COMMENT '错误信息',
    `cost_time` BIGINT DEFAULT NULL COMMENT '耗时(ms)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
