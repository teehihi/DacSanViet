-- Add product_image_url to order_items table for email display
ALTER TABLE order_items ADD COLUMN product_image_url VARCHAR(500) AFTER category_name;
