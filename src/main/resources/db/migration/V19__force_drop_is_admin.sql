-- Force drop is_admin column
ALTER TABLE users DROP COLUMN IF EXISTS is_admin;
