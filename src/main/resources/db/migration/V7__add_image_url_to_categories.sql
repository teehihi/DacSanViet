-- Add image_url column to categories table
ALTER TABLE categories ADD COLUMN image_url VARCHAR(500);

-- Update existing categories with default images
UPDATE categories SET image_url = 'https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=400&q=80' WHERE name LIKE '%Bánh%' OR name LIKE '%Kẹo%' OR name LIKE '%Mứt%';
UPDATE categories SET image_url = 'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400&q=80' WHERE name LIKE '%Khô%';
UPDATE categories SET image_url = 'https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=400&q=80' WHERE name LIKE '%Rượu%';
UPDATE categories SET image_url = 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400&q=80' WHERE name LIKE '%Nem%' OR name LIKE '%Chả%' OR name LIKE '%Lạp%';
UPDATE categories SET image_url = 'https://images.unsplash.com/photo-1518843875459-f738682238a6?w=400&q=80' WHERE name LIKE '%Rau%' OR name LIKE '%Gạo%' OR name LIKE '%Nấm%';
UPDATE categories SET image_url = 'https://images.unsplash.com/photo-1596040033229-a0b3b83b6c0f?w=400&q=80' WHERE name LIKE '%Gia vị%';
UPDATE categories SET image_url = 'https://images.unsplash.com/photo-1535140728325-a4d3707eee61?w=400&q=80' WHERE name LIKE '%Thịt%' OR name LIKE '%Cá%';
UPDATE categories SET image_url = 'https://images.unsplash.com/photo-1505751172876-fa1923c5c528?w=400&q=80' WHERE name LIKE '%Sức khỏe%';

-- Set default image for any remaining categories
UPDATE categories SET image_url = 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400&q=80' WHERE image_url IS NULL;
