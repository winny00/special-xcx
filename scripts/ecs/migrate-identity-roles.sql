-- identity-roles: 预约挂老师档案（可重复执行，兼容 MySQL 5.7+）
-- 本任务只加列；菜单放到 Task 9
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

CALL special_add_column('special_appointment', 'teacher_id',
  'ALTER TABLE special_appointment ADD COLUMN teacher_id bigint(20) DEFAULT NULL COMMENT ''老师档案ID'' AFTER user_id');

DROP PROCEDURE IF EXISTS special_modify_column;
DELIMITER $$
CREATE PROCEDURE special_modify_column(IN p_table varchar(64), IN p_column varchar(64), IN p_ddl varchar(1000))
BEGIN
  SET @is_nullable := (
    SELECT IS_NULLABLE FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
  );
  IF @is_nullable = 'NO' THEN
    SET @sql := p_ddl;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL special_modify_column('special_appointment', 'resource_id',
  'ALTER TABLE special_appointment MODIFY COLUMN resource_id bigint(20) DEFAULT NULL COMMENT ''关联资源ID''');

DROP PROCEDURE IF EXISTS special_add_column;
DROP PROCEDURE IF EXISTS special_modify_column;

-- Task 9: 用户角色菜单（可重复执行）
INSERT INTO sys_menu VALUES (1764000000000000009, '用户角色', 1764000000000000001, 6, 'account', 'special/account/index', '', 'N', 'Y', 'C', '0', '0', 'special:account:list', 'peoples', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '用户角色';
INSERT INTO sys_menu VALUES (1764000000000000081, '用户角色查询', 1764000000000000009, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:account:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '用户角色查询';
INSERT INTO sys_menu VALUES (1764000000000000082, '用户角色编辑', 1764000000000000009, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:account:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '用户角色编辑';

-- 给 special_teacher 挂老师档案查询/修改/列表 + 预约列表/查询/处理。不挂 account/resource/org/parent/audit
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1763000000000000002, 1764000000000000071 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1763000000000000002 AND menu_id = 1764000000000000071);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1763000000000000002, 1764000000000000073 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1763000000000000002 AND menu_id = 1764000000000000073);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1763000000000000002, 1764000000000000075 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1763000000000000002 AND menu_id = 1764000000000000075);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1763000000000000002, 1764000000000000004 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1763000000000000002 AND menu_id = 1764000000000000004);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1763000000000000002, 1764000000000000031 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1763000000000000002 AND menu_id = 1764000000000000031);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1763000000000000002, 1764000000000000032 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1763000000000000002 AND menu_id = 1764000000000000032);

