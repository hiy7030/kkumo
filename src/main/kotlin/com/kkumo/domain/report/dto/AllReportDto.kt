package com.kkumo.domain.report.dto

import java.time.LocalDate

/**
 * 일별 요약 카드 데이터
 */
data class DailySummaryDto(
    val date: LocalDate,
    val dayOfWeek: String, // 요일 (월, 화, 수...)
    val totalMemberCount: Int, // 전체 멤버 수
    val postedMembers: List<MemberDto>, // 기록한 멤버들
    val notPostedMembers: List<MemberDto> // 기록 안 한 멤버들
)

/**
 * 멤버 정보
 */
data class MemberDto(
    val nickname: String,
    val emoji: String
)
