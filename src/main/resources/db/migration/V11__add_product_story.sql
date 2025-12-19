-- Add story fields to products table
ALTER TABLE products 
    ADD COLUMN story TEXT NULL COMMENT 'Product story content (can contain HTML from rich text editor)',
    ADD COLUMN story_image_url VARCHAR(500) NULL COMMENT 'Story section image URL';
