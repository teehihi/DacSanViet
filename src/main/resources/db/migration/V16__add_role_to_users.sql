-- Add role column to users table
ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER';

-- Migrate existing data: is_admin=true -> ADMIN, is_admin=false -> USER
UPDATE users SET role = 'ADMIN' WHERE is_admin = true;
UPDATE users SET role = 'USER' WHERE is_admin = false OR is_admin IS NULL;

-- Drop the old is_admin column
ALTER TABLE users DROP COLUMN is_admin;

-- Add index for role
CREATE INDEX idx_users_role ON users(role);
