-- ==================================================================================
-- Spring Security Remember-Me (Persistent Token) 테이블 생성
-- ==================================================================================
-- 이 테이블은 Spring Security의 JdbcTokenRepositoryImpl이 사용하는 표준 스키마입니다.
-- Remember-Me 토큰을 데이터베이스에 영구 저장하여 자동 로그인을 구현합니다.
-- ==================================================================================

CREATE TABLE IF NOT EXISTS persistent_logins (
    username VARCHAR(64) NOT NULL COMMENT '사용자 아이디 (이메일)',
    series VARCHAR(64) PRIMARY KEY COMMENT '토큰 시리즈 (고유 식별자)',
    token VARCHAR(64) NOT NULL COMMENT '실제 Remember-Me 토큰 값',
    last_used TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 사용 시간'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Security Remember-Me 토큰 저장소';

-- 인덱스 생성 (성능 최적화)
CREATE INDEX idx_username ON persistent_logins(username);
