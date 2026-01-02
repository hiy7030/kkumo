package com.kkumo.global.utils

/**
 * 이모지 처리를 위한 유틸리티 클래스
 */
object EmojiUtils {

    /**
     * 이모지 정규화: Variant Selector 및 불필요한 문자 제거
     *
     * 문제 상황:
     * - 같은 이모지가 Unicode에서 여러 형태로 존재 가능
     *   예: 🍐 = U+1F350 (기본형) 또는 U+1F350 U+FE0F (Variant Selector 포함)
     * - 브라우저/키보드마다 다른 형태로 입력될 수 있음
     * - DB Unique 제약조건과 JPA 쿼리 결과가 불일치할 수 있음
     *
     * 해결 방법:
     * - Variant Selector(U+FE0F) 제거
     * - 공백 및 제어 문자 제거
     * - 모든 이모지를 정규화된 형태로 통일
     *
     * @param emoji 정규화할 이모지 문자열
     * @return 정규화된 이모지
     */
    fun normalize(emoji: String): String {
        return emoji
            .replace("\uFE0F", "")  // Variant Selector-16 제거
            .replace("\uFE0E", "")  // Variant Selector-15 제거
            .trim()                  // 공백 제거
    }
}
