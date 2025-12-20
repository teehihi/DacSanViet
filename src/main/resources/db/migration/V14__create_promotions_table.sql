-- Create promotions table
CREATE TABLE promotions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    discount_type VARCHAR(20) NOT NULL, -- 'PERCENTAGE' or 'FIXED_AMOUNT'
    discount_value DECIMAL(10, 2) NOT NULL,
    min_order_value DECIMAL(10, 2) DEFAULT 0,
    max_discount_amount DECIMAL(10, 2),
    usage_limit INT,
    used_count INT DEFAULT 0,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_active_dates (is_active, start_date, end_date)
);

-- Insert sample promotions
INSERT INTO promotions (code, description, discount_type, discount_value, min_order_value, max_discount_amount, usage_limit, start_date, end_date, is_active) VALUES
('WELCOME10', 'Giảm 10% cho đơn hàng đầu tiên', 'PERCENTAGE', 10.00, 100000, 50000, 1000, '2024-01-01 00:00:00', '2025-12-31 23:59:59', TRUE),
('FREESHIP', 'Miễn phí vận chuyển', 'FIXED_AMOUNT', 30000, 200000, 30000, NULL, '2024-01-01 00:00:00', '2025-12-31 23:59:59', TRUE),
('SUMMER50K', 'Giảm 50K cho đơn từ 500K', 'FIXED_AMOUNT', 50000, 500000, 50000, 500, '2024-01-01 00:00:00', '2025-12-31 23:59:59', TRUE),
('VIP20', 'Giảm 20% cho khách VIP', 'PERCENTAGE', 20.00, 300000, 100000, NULL, '2024-01-01 00:00:00', '2025-12-31 23:59:59', TRUE);
