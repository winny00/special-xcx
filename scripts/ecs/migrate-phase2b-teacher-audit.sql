-- phase2b 老师档案 + 审核备注（可重复执行，兼容 MySQL 5.7+）
SET @db := DATABASE();

DROP PROCEDURE IF EXISTS special_add_column;
DELIMITER $$
CREATE PROCEDURE special_add_column(IN p_table varchar(64), IN p_column varchar(64), IN p_ddl varchar(1000))
BEGIN
  SET @exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
  );
  IF @exists = 0 THEN
    SET @sql := p_ddl;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL special_add_column('special_organization', 'audit_remark',
  'ALTER TABLE special_organization ADD COLUMN audit_remark varchar(500) DEFAULT NULL COMMENT ''审核备注'' AFTER audit_status');
CALL special_add_column('special_organization', 'audit_by',
  'ALTER TABLE special_organization ADD COLUMN audit_by bigint(20) DEFAULT NULL COMMENT ''审核人'' AFTER audit_remark');
CALL special_add_column('special_organization', 'audit_time',
  'ALTER TABLE special_organization ADD COLUMN audit_time datetime DEFAULT NULL COMMENT ''审核时间'' AFTER audit_by');

CALL special_add_column('special_resource', 'audit_remark',
  'ALTER TABLE special_resource ADD COLUMN audit_remark varchar(500) DEFAULT NULL COMMENT ''审核备注'' AFTER status');
CALL special_add_column('special_resource', 'audit_by',
  'ALTER TABLE special_resource ADD COLUMN audit_by bigint(20) DEFAULT NULL COMMENT ''审核人'' AFTER audit_remark');
CALL special_add_column('special_resource', 'audit_time',
  'ALTER TABLE special_resource ADD COLUMN audit_time datetime DEFAULT NULL COMMENT ''审核时间'' AFTER audit_by');

DROP PROCEDURE IF EXISTS special_add_column;

CREATE TABLE IF NOT EXISTS special_teacher (
    id              bigint(20)      NOT NULL                    COMMENT '主键',
    user_id         bigint(20)      DEFAULT NULL                COMMENT '关联用户',
    name            varchar(100)    NOT NULL                    COMMENT '姓名',
    title           varchar(100)    DEFAULT NULL                COMMENT '职称头衔',
    specialties     varchar(500)    DEFAULT NULL                COMMENT '擅长领域',
    qualification   varchar(500)    DEFAULT NULL                COMMENT '资质说明',
    cert_image_url  varchar(500)    DEFAULT NULL                COMMENT '证书图',
    avatar_url      varchar(500)    DEFAULT NULL                COMMENT '头像',
    org_id          bigint(20)      DEFAULT NULL                COMMENT '所属机构',
    intro           text                                        COMMENT '简介',
    status          tinyint         DEFAULT 0                   COMMENT '状态 0待审 1通过 2拒绝',
    resource_id     bigint(20)      DEFAULT NULL                COMMENT '关联资源',
    audit_remark    varchar(500)    DEFAULT NULL                COMMENT '审核备注',
    audit_by        bigint(20)      DEFAULT NULL                COMMENT '审核人',
    audit_time      datetime        DEFAULT NULL                COMMENT '审核时间',
    create_dept     bigint(20)      DEFAULT NULL                COMMENT '创建部门',
    create_by       bigint(20)      DEFAULT NULL                COMMENT '创建者',
    create_time     datetime        DEFAULT NULL                COMMENT '创建时间',
    update_by       bigint(20)      DEFAULT NULL                COMMENT '更新者',
    update_time     datetime        DEFAULT NULL                COMMENT '更新时间',
    del_flag        char(1)         DEFAULT '0'                 COMMENT '删除标志',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='特教老师档案表';

INSERT INTO sys_menu VALUES (1764000000000000008, '老师档案', 1764000000000000001, 5, 'teacher', 'special/teacher/index', '', 'N', 'Y', 'C', '0', '0', 'special:teacher:list', 'peoples', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师档案';
INSERT INTO sys_menu VALUES (1764000000000000071, '老师查询', 1764000000000000008, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:teacher:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师查询';
INSERT INTO sys_menu VALUES (1764000000000000072, '老师新增', 1764000000000000008, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:teacher:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师新增';
INSERT INTO sys_menu VALUES (1764000000000000073, '老师修改', 1764000000000000008, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:teacher:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师修改';
INSERT INTO sys_menu VALUES (1764000000000000074, '老师删除', 1764000000000000008, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:teacher:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师删除';
INSERT INTO sys_menu VALUES (1764000000000000075, '老师列表', 1764000000000000008, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:teacher:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师列表';
