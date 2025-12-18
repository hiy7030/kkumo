package com.kkumo.domain.report.controller

import com.kkumo.domain.member.Member
import com.kkumo.domain.post.repository.PostRepository
import com.kkumo.domain.report.service.ReportService
import com.kkumo.global.annotation.KKumoWebController
import com.kkumo.global.auth.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@KKumoWebController
class ReportController(
    private val postRepository: PostRepository,
    private val reportService: ReportService
) {

    @GetMapping("/reports/my")
    fun myCalendar(
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        model: Model
    ): String {
        val member = userDetails.member

        // 현재 연/월 또는 요청된 연/월 사용
        val targetYearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        val targetYear = targetYearMonth.year
        val targetMonth = targetYearMonth.monthValue

        // 해당 월의 시작일과 종료일
        val startDate = targetYearMonth.atDay(1)
        val endDate = targetYearMonth.atEndOfMonth()

        // 해당 월의 게시글 조회
        val posts = postRepository.findAllByMemberAndPostedDateBetween(member, startDate, endDate)
        val postedDates = posts.map { it.postedDate }.toSet()

        // 달력 데이터 생성
        val calendarDays = mutableListOf<CalendarDay>()

        // 1일의 요일 (1=월요일, 7=일요일) -> 0=일요일로 변환
        val firstDayOfWeek = startDate.dayOfWeek.value % 7

        // 빈칸 추가 (일요일부터 시작)
        repeat(firstDayOfWeek) {
            calendarDays.add(CalendarDay(day = 0, hasPost = false, date = null))
        }

        // 실제 날짜 추가
        for (day in 1..targetYearMonth.lengthOfMonth()) {
            val date = targetYearMonth.atDay(day)
            val hasPost = date in postedDates
            calendarDays.add(CalendarDay(day = day, hasPost = hasPost, date = date))
        }

        // 이전/다음 월 계산
        val prevYearMonth = targetYearMonth.minusMonths(1)
        val nextYearMonth = targetYearMonth.plusMonths(1)

        // 현재 월인지 확인
        val currentYearMonth = YearMonth.now()
        val isCurrentMonth = targetYearMonth == currentYearMonth
        val hasPostedToday = userDetails.member.lastPostedAt == LocalDate.now()

        // 모델에 데이터 전달
        model.addAttribute("year", targetYear)
        model.addAttribute("month", targetMonth)
        model.addAttribute("calendarDays", calendarDays)
        model.addAttribute("userEmoji", member.myEmoji)
        model.addAttribute("prevYear", prevYearMonth.year)
        model.addAttribute("prevMonth", prevYearMonth.monthValue)
        model.addAttribute("nextYear", nextYearMonth.year)
        model.addAttribute("nextMonth", nextYearMonth.monthValue)
        model.addAttribute("today", LocalDate.now())
        model.addAttribute("hasPostedToday", hasPostedToday)
        model.addAttribute("isCurrentMonth", isCurrentMonth)

        return "my-report"
    }

    @GetMapping("/reports/all")
    fun allReport(
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        model: Model
    ): String {
        // 현재 연/월 또는 요청된 연/월 사용
        val targetYearMonth = if (year != null && month != null) {
            YearMonth.of(year, month)
        } else {
            YearMonth.now()
        }

        val targetYear = targetYearMonth.year
        val targetMonth = targetYearMonth.monthValue

        // 일별 요약 데이터 생성
        val dailySummaries = reportService.generateDailySummaries(targetYear, targetMonth)

        // 이전/다음 월 계산
        val prevYearMonth = targetYearMonth.minusMonths(1)
        val nextYearMonth = targetYearMonth.plusMonths(1)

        // 현재 월인지 확인
        val currentYearMonth = YearMonth.now()
        val isCurrentMonth = targetYearMonth == currentYearMonth
        val hasPostedToday = userDetails.member.lastPostedAt == LocalDate.now()

        // 모델에 데이터 전달
        model.addAttribute("year", targetYear)
        model.addAttribute("month", targetMonth)
        model.addAttribute("dailySummaries", dailySummaries)
        model.addAttribute("prevYear", prevYearMonth.year)
        model.addAttribute("prevMonth", prevYearMonth.monthValue)
        model.addAttribute("nextYear", nextYearMonth.year)
        model.addAttribute("nextMonth", nextYearMonth.monthValue)
        model.addAttribute("isCurrentMonth", isCurrentMonth)
        model.addAttribute("hasPostedToday", hasPostedToday)

        return "all-report"
    }

    /**
     * 달력 날짜 데이터 클래스
     * @param day 일자 (0이면 빈칸)
     * @param hasPost 게시글 존재 여부
     * @param date 실제 LocalDate (빈칸인 경우 null)
     */
    data class CalendarDay(
        val day: Int,
        val hasPost: Boolean,
        val date: LocalDate?
    )
}