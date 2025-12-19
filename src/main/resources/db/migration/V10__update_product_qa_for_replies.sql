-- Update product_qa table to support nested replies and likes
ALTER TABLE product_qa 
    ADD COLUMN parent_id BIGINT NULL AFTER id,
    ADD COLUMN likes_count INT DEFAULT 0,
    ADD CONSTRAINT fk_product_qa_parent FOREIGN KEY (parent_id) 
        REFERENCES product_qa(id) ON DELETE CASCADE;

-- Create table for tracking who liked which Q&A
CREATE TABLE IF NOT EXISTS product_qa_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    qa_id BIGINT NOT NULL,
    user_identifier VARCHAR(255) NOT NULL COMMENT 'Email or session ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_qa_like (qa_id, user_identifier),
    CONSTRAINT fk_qa_likes_qa FOREIGN KEY (qa_id) 
        REFERENCES product_qa(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add index for better performance
CREATE INDEX idx_parent_id ON product_qa(parent_id);
CREATE INDEX idx_qa_likes_qa_id ON product_qa_likes(qa_id);
