-- Add is_private column to posts table
ALTER TABLE posts
ADD COLUMN is_private BOOLEAN NOT NULL DEFAULT FALSE
COMMENT '비공개 여부 (이미지 블러 처리)';

-- Add index for query performance
CREATE INDEX idx_post_is_private ON posts(is_private);
