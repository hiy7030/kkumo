package com.kkumo.domain.report.service

import com.kkumo.domain.member.repository.MemberRepository
import com.kkumo.domain.post.repository.PostRepository
import com.kkumo.domain.report.dto.DailySummaryDto
import com.kkumo.domain.report.dto.MemberDto
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Service
class ReportService(
    private val memberRepository: MemberRepository,
    private val postRepository: PostRepository,
    private val kkumoProperties: com.kkumo.global.config.KkumoProperties,
) {

    /**
     * 일별 요약 카드 리스트 생성 (모바일 친화적)
     * @param year 연도
     * @param month 월
     * @return 일별 요약 리스트 (최신 날짜순)
     */
    fun generateDailySummaries(year: Int, month: Int): List<DailySummaryDto> {
        val targetYearMonth = YearMonth.of(year, month)
        val today = LocalDate.now()

        // 1. 조회 범위 계산 (기존 로직 유지)
        val baseDate = kkumoProperties.baseDate
        val initialStartDate = targetYearMonth.atDay(1)

        // 시작일: 해당 월 1일과 서비스 시작일 중 늦은 날짜
        val startDate = if (initialStartDate.isBefore(baseDate)) baseDate else initialStartDate

        // 종료일: 해당 월 마지막 날과 오늘 중 이른 날짜 (미래 데이터 조회 방지)
        val endDate = targetYearMonth.atEndOfMonth()
        val limitDate = if (endDate.isAfter(today)) today else endDate

        // 예외 처리: 시작일이 종료일보다 미래인 경우 (아직 오지 않은 달) -> 빈 리스트
        if (startDate.isAfter(limitDate)) {
            return emptyList()
        }

        // 2. 모든 멤버 조회 (기존 sortedBy 유지 - Repository 수정 없이 안전하게)
        val allMembers = memberRepository.findAll().sortedBy { it.nickname }
        val totalMemberCount = allMembers.size

        // 3. 해당 기간의 모든 게시글 조회
        val posts = postRepository.findAllByPostedDateBetween(startDate, limitDate)

        // 4. 날짜별 작성 멤버 ID 맵핑 (최적화)
        // Map<LocalDate, Set<Long>> 형태로 변환하여 빠른 조회
        val dateToMemberIdsMap = posts.groupBy { it.postedDate }
            .mapValues { (_, dayPosts) ->
                dayPosts.mapNotNull { it.member.id }.toSet()
            }

        // 5. 일별 요약 생성 (IntRange 활용)
        val startDay = startDate.dayOfMonth
        val endDay = limitDate.dayOfMonth

        // startDay부터 endDay까지 역순으로 반복
        return (startDay..endDay).reversed().map { day ->
            val currentDate = targetYearMonth.atDay(day)
            val postedMemberIds = dateToMemberIdsMap[currentDate] ?: emptySet()

            // [핵심 리팩토링] partition: filter를 두 번 돌지 않고, 한 번에 두 그룹으로 분리
            val (postedMembersEntities, notPostedMembersEntities) = allMembers.partition {
                it.id in postedMemberIds
            }

            DailySummaryDto(
                date = currentDate,
                dayOfWeek = currentDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                totalMemberCount = totalMemberCount,
                // Named Argument 복구 (순서 상관없이 안전하게)
                postedMembers = postedMembersEntities.map {
                    MemberDto(nickname = it.nickname, emoji = it.myEmoji)
                },
                notPostedMembers = notPostedMembersEntities.map {
                    MemberDto(nickname = it.nickname, emoji = it.myEmoji)
                }
            )
        }
    }
}
