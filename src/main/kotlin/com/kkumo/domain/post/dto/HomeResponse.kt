package com.kkumo.domain.post.dto

import com.kkumo.domain.post.Post
import com.kkumo.domain.reaction.ReactionType
import java.time.LocalDateTime

/**
 * 홈 피드용 응답 DTO
 * HomeController에서 피드 리스트를 표시하기 위한 데이터 전송 객체
 */
object HomeResponse {
    data class HomeResponse(
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
        val reactions: Map<ReactionType, Int>  // ReactionType Enum -> 개수
    ) {
        companion object {
            /**
             * Post 엔티티로부터 FeedResponse 생성
             * @param post Post 엔티티
             * @param reactions 해당 Post의 리액션 개수 맵
             */
            fun from(post: Post, reactions: Map<ReactionType, Int>): FeedResponse {
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

    data class MemberResponse(
        val currentStreak: Int,
        val hasCrown: Boolean,
        val hasPostedToday: Boolean,
    )
}

