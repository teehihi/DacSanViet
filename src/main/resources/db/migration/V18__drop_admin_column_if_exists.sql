-- Drop is_admin column if it still exists
-- This is a safety migration in case V16 didn't run properly

-- Check and drop is_admin column if exists (MySQL syntax)
SET @dbname = DATABASE();
SET @tablename = 'users';
SET @columnname = 'is_admin';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'ALTER TABLE users DROP COLUMN is_admin;',
  'SELECT 1;'
));

PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;
