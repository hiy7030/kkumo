package com.kkumo.global.scheduler

import com.kkumo.domain.member.repository.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class StreakScheduler(
    private val memberRepository: MemberRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 매일 자정(00:00 KST)에 실행되는 Streak 초기화 스케줄러
     * 어제 기록을 남기지 않은 회원들의 Streak와 Crown을 초기화한다.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    fun resetStreakForInactiveMembers() {
        val yesterday = LocalDate.now().minusDays(1)

        logger.info("[StreakScheduler] Starting streak reset for members who did not post on: {}", yesterday)

        val updatedCount = memberRepository.bulkResetStreakNotPostedAt(yesterday)

        logger.info(
            "[StreakScheduler] Streak reset completed. {} member(s) had their streak and crown reset.",
            updatedCount
        )
    }
}