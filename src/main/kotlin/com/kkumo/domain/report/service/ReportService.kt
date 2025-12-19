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
        val initialStartDate = targetYearMonth.atDay(1)
        val endDate = targetYearMonth.atEndOfMonth()

        // Base Date Clamping: startDate가 baseDate보다 이전이면 baseDate로 대체
        val baseDate = kkumoProperties.baseDate
        val startDate = if (initialStartDate.isBefore(baseDate)) baseDate else initialStartDate

        // 현재 월인 경우 오늘까지만, 과거 월인 경우 마지막 날까지
        val currentYearMonth = YearMonth.now()
        val limitDate = if (targetYearMonth == currentYearMonth) {
            LocalDate.now()
        } else {
            endDate
        }

        // 1. 모든 멤버 조회 (닉네임 기준 정렬)
        val allMembers = memberRepository.findAll().sortedBy { it.nickname }
        val totalMemberCount = allMembers.size

        // 2. 해당 기간의 모든 게시글 조회 (Base Date 이후 데이터만)
        val posts = postRepository.findAllByPostedDateBetween(startDate, limitDate)

        // 3. 날짜별 작성 멤버 맵 구성
        // Map<LocalDate, Set<MemberId>>
        val dateToMemberIdsMap = posts.groupBy { it.postedDate }
            .mapValues { entry -> entry.value.map { it.member.id }.toSet() }

        // 4. 일별 요약 생성 (최신 날짜순)
        val summaries = (1..limitDate.dayOfMonth).reversed().map { day ->
            val currentDate = targetYearMonth.atDay(day)
            val postedMemberIds = dateToMemberIdsMap[currentDate] ?: emptySet()

            // 기록한 멤버
            val postedMembers = allMembers
                .filter { it.id in postedMemberIds }
                .map { MemberDto(nickname = it.nickname, emoji = it.myEmoji) }

            // 기록 안 한 멤버
            val notPostedMembers = allMembers
                .filter { it.id !in postedMemberIds }
                .map { MemberDto(nickname = it.nickname, emoji = it.myEmoji) }

            // 요일 추출 (한글)
            val dayOfWeek = currentDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)

            DailySummaryDto(
                date = currentDate,
                dayOfWeek = dayOfWeek,
                totalMemberCount = totalMemberCount,
                postedMembers = postedMembers,
                notPostedMembers = notPostedMembers
            )
        }

        return summaries
    }
}
