-- Allow null user_id in orders table for guest checkout
ALTER TABLE orders MODIFY COLUMN user_id BIGINT NULL;

-- Also update the foreign key constraint if it exists
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FKel9kyl84ego2otj2accfd8mr7;
ALTER TABLE orders ADD CONSTRAINT FKel9kyl84ego2otj2accfd8mr7 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
