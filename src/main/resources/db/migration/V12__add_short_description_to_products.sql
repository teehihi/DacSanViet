-- Add short_description column to products table
ALTER TABLE products ADD COLUMN short_description TEXT;

-- Update existing products with short description from description
UPDATE products 
SET short_description = SUBSTRING(description, 1, 200)
WHERE description IS NOT NULL;
