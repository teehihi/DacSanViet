-- Fix user roles - set all non-admin users to USER role
-- Only keep admin user as ADMIN
UPDATE users 
SET role = 'USER' 
WHERE username != 'admin' AND role = 'ADMIN';

-- Ensure admin user has ADMIN role
UPDATE users 
SET role = 'ADMIN' 
WHERE username = 'admin';
