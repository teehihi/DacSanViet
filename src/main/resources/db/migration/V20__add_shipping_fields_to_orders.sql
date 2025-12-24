-- Add shipping carrier and shipping method to orders table
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_method VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_carrier VARCHAR(100);

-- Drop unnecessary columns to simplify
ALTER TABLE orders DROP COLUMN IF EXISTS shipping_address_id;
ALTER TABLE orders DROP COLUMN IF EXISTS tax_amount;
ALTER TABLE orders DROP COLUMN IF EXISTS delivery_confirmed_at;

-- Add comments
COMMENT ON COLUMN orders.shipping_method IS 'Shipping method chosen by customer (e.g., STANDARD, EXPRESS_5H)';
COMMENT ON COLUMN orders.shipping_carrier IS 'Shipping carrier/company (e.g., DacSanVietShip, GHN, GHTK, Viettel Post)';
