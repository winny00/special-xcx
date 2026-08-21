-- phase2a ECS 增量（可重复执行，兼容 MySQL 5.7+）
-- 机构封面（MySQL 5.7 不支持 ADD COLUMN IF NOT EXISTS）
SET @db := DATABASE();
SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db
    AND TABLE_NAME = 'special_organization'
    AND COLUMN_NAME = 'cover_url'
);
SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE special_organization ADD COLUMN cover_url varchar(500) DEFAULT NULL COMMENT ''封面图'' AFTER description',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 资讯表
CREATE TABLE IF NOT EXISTS special_article (
    id              bigint(20)      NOT NULL                    COMMENT '主键',
    title           varchar(200)    NOT NULL                    COMMENT '标题',
    summary         varchar(500)    DEFAULT NULL                COMMENT '摘要',
    content         text                                        COMMENT '正文 HTML',
    cover_url       varchar(500)    DEFAULT NULL                COMMENT '封面图',
    category        varchar(32)     DEFAULT NULL                COMMENT '分类 policy/news/guide',
    status          tinyint         DEFAULT 0                   COMMENT '状态 0草稿 1已发布 2已下架',
    publish_time    datetime        DEFAULT NULL                COMMENT '发布时间',
    view_count      int(11)         DEFAULT 0                   COMMENT '浏览量',
    create_dept     bigint(20)      DEFAULT NULL                COMMENT '创建部门',
    create_by       bigint(20)      DEFAULT NULL                COMMENT '创建者',
    create_time     datetime        DEFAULT NULL                COMMENT '创建时间',
    update_by       bigint(20)      DEFAULT NULL                COMMENT '更新者',
    update_time     datetime        DEFAULT NULL                COMMENT '更新时间',
    del_flag        char(1)         DEFAULT '0'                 COMMENT '删除标志',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='特教资讯政策表';

INSERT INTO sys_menu VALUES (1764000000000000006, '资讯管理', 1764000000000000001, 3, 'article', 'special/article/index', '', 'N', 'Y', 'C', '0', '0', 'special:article:list', 'documentation', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资讯管理';
INSERT INTO sys_menu VALUES (1764000000000000051, '资讯查询', 1764000000000000006, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:article:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资讯查询';
INSERT INTO sys_menu VALUES (1764000000000000052, '资讯新增', 1764000000000000006, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:article:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资讯新增';
INSERT INTO sys_menu VALUES (1764000000000000053, '资讯修改', 1764000000000000006, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:article:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资讯修改';
INSERT INTO sys_menu VALUES (1764000000000000054, '资讯删除', 1764000000000000006, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:article:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资讯删除';

INSERT INTO special_article VALUES
(1768000000000000001, '广东省特殊教育提升计划解读', '梳理省内特教资源布局与入学支持政策要点。', '<p>本文介绍广东省特殊教育提升计划的核心目标：扩大特教学位供给、加强融合教育支持、完善评估与转衔服务。</p><p>家长可关注当地教育局发布的入学指南与康复补贴申请渠道。</p>', null, 'policy', 1, sysdate(), 128, 1761000000000000103, 1761100000000000001, sysdate(), null, null, '0'),
(1768000000000000002, '自闭症儿童家庭支持服务指南', '为新手家长整理评估、干预与社区资源对接路径。', '<p>建议家长优先完成发育评估，再根据评估结果选择感统、语言或行为干预课程。</p><p>本平台可预约咨询，对接机构与老师资源。</p>', null, 'guide', 1, sysdate(), 86, 1761000000000000103, 1761100000000000001, sysdate(), null, null, '0'),
(1768000000000000003, '平台上线：特教资源一站式预约', '课程、老师、机构资源集中展示，支持在线预约咨询。', '<p>特教资源平台现已上线，家长可在首页浏览推荐资源并提交预约。</p>', null, 'news', 1, sysdate(), 42, 1761000000000000103, 1761100000000000001, sysdate(), null, null, '0')
ON DUPLICATE KEY UPDATE title=VALUES(title);
