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
