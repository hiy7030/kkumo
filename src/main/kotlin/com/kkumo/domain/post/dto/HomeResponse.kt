package com.kkumo.domain.post.dto

import com.kkumo.domain.post.Post
import com.kkumo.domain.reaction.ReactionType
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 홈 피드용 응답 DTO
 * HomeController에서 피드 리스트를 표시하기 위한 데이터 전송 객체
 */
object HomeResponse {
    data class HomeResponse(
        val selectedDate: LocalDate,
        val isToday: Boolean,
        val memberInfos: MemberResponse,
        val posts: List<FeedResponse>
    )

    data class FeedResponse(
        val id: Long,
        val nickname: String,
        val writerEmoji: String,
        val hasCrown: Boolean,  // 왕관 보유 여부 (7일 연속 달성자)
        val thumbnailUrl: String,  // 목록 표시용 썸네일 URL
        val originalImageUrl: String,  // 상세 조회용 원본 이미지 URL
        val comment: String,
        val createdAt: LocalDateTime,  // 작성 시간
        val reactions: Map<ReactionType, ReactionInfo>  // ReactionType Enum -> 리액션 정보
    ) {
        companion object {
            /**
             * Post 엔티티로부터 FeedResponse 생성
             * @param post Post 엔티티
             * @param reactions 해당 Post의 리액션 정보 맵
             */
            fun from(post: Post, reactions: Map<ReactionType, ReactionInfo>): FeedResponse {
                return FeedResponse(
                    id = post.id ?: 0L,
                    nickname = post.member.nickname,
                    writerEmoji = post.member.myEmoji,
                    hasCrown = post.member.hasCrown,
                    thumbnailUrl = post.thumbnailUrl,
                    originalImageUrl = post.originalImageUrl,
                    comment = post.content ?: "",
                    createdAt = post.createdAt,
                    reactions = reactions
                )
            }
        }
    }

    /**
     * 리액션 상세 정보
     * @param count 리액션 총 개수
     * @param recentReactors 최근 반응한 사용자 닉네임 리스트 (최대 3명, 최신순)
     */
    data class ReactionInfo(
        val count: Int,
        val recentReactors: List<String> = emptyList()
    )

    data class MemberResponse(
        val currentStreak: Int,
        val hasCrown: Boolean,
        val hasPostedToday: Boolean,
    )
}

