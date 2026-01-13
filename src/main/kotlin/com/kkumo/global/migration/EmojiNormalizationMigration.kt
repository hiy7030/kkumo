package com.kkumo.global.migration

import com.kkumo.domain.member.repository.MemberRepository
import com.kkumo.global.utils.EmojiUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * ✅ [마이그레이션 완료] 이모지 정규화 마이그레이션
 *
 * 상태: COMPLETED (2026-01-13)
 * - 모든 회원 이모지 정규화 완료
 * - 중복 문제 해결 완료
 * - 현재 비활성화 상태 (enabled: false)
 *
 * ⚠️ 이 파일은 참고용으로만 유지되며, 더 이상 실행되지 않습니다.
 * 필요시 Git 히스토리에서 복구 가능하므로 삭제해도 무방합니다.
 *
 * ---
 *
 * 목적:
 * - 기존 DB에 저장된 모든 회원의 이모지를 정규화
 * - Variant Selector(U+FE0F, U+FE0E) 제거
 * - 중복 문제 해결
 *
 * 실행 방법:
 * 1. application.yml에 다음 설정 추가:
 *    migration:
 *      emoji-normalization:
 *        enabled: true
 * 2. 애플리케이션 재시작
 * 3. 마이그레이션 완료 후 enabled: false로 변경
 *
 * 주의:
 * - 한 번만 실행되도록 설정 후 반드시 비활성화할 것
 * - 실행 전 반드시 DB 백업 권장
 */
@Component
class EmojiNormalizationMigration(
    private val memberRepository: MemberRepository,
    @Value("\${migration.emoji-normalization.enabled:false}")
    private val enabled: Boolean
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(vararg args: String?) {
        if (!enabled) {
            logger.info("[EmojiNormalizationMigration] 마이그레이션이 비활성화되어 있습니다.")
            return
        }

        logger.info("[EmojiNormalizationMigration] 시작: 모든 회원의 이모지를 정규화합니다.")

        try {
            val members = memberRepository.findAll()
            var normalizedCount = 0

            members.forEach { member ->
                val originalEmoji = member.myEmoji
                val normalizedEmoji = EmojiUtils.normalize(originalEmoji)

                if (originalEmoji != normalizedEmoji) {
                    member.myEmoji = normalizedEmoji
                    normalizedCount++
                    logger.debug(
                        "[EmojiNormalizationMigration] 정규화 완료 - " +
                        "회원: ${member.nickname}, " +
                        "원본: ${originalEmoji.toByteArray().joinToString { "%02X".format(it) }}, " +
                        "정규화: ${normalizedEmoji.toByteArray().joinToString { "%02X".format(it) }}"
                    )
                }
            }

            memberRepository.saveAll(members)

            logger.info(
                "[EmojiNormalizationMigration] 완료 - " +
                "전체: ${members.size}명, " +
                "정규화: ${normalizedCount}명"
            )

            if (normalizedCount > 0) {
                logger.warn(
                    "[EmojiNormalizationMigration] 주의: " +
                    "마이그레이션이 완료되었습니다. " +
                    "application.yml에서 'migration.emoji-normalization.enabled'를 false로 변경하세요."
                )
            }

        } catch (e: Exception) {
            logger.error("[EmojiNormalizationMigration] 실패: ${e.message}", e)
            throw e
        }
    }
}
