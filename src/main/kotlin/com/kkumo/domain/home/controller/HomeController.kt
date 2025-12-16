package com.kkumo.domain.home.controller

import com.kkumo.domain.post.repository.PostRepository
import com.kkumo.domain.post.service.PostService
import com.kkumo.domain.reaction.ReactionType
import com.kkumo.global.annotation.KKumoWebController
import com.kkumo.global.auth.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDate
import java.time.LocalDateTime

@KKumoWebController
class HomeController(
    private val postRepository: PostRepository,
    private val postService: PostService
) {

    @GetMapping("/home")
    fun homePage(
        @AuthenticationPrincipal user: CustomUserDetails,
        model: Model
    ): String {
        // ==================== 로그인 유저 상태 (실제 데이터) ====================
        val member = user.member
        val today = LocalDate.now()

        // 오늘 기록 여부 체크
        val hasPostedToday = postRepository.existsByMemberAndPostedDate(member, today)
        model.addAttribute("hasPostedToday", hasPostedToday)

        // 현재 Streak 연속 일수 (Member 엔티티에서 가져오기)
        val currentStreak = member.currentStreak
        model.addAttribute("currentStreak", currentStreak)

        // 왕관 보유 여부 (Member 엔티티에서 가져오기)
        val hasCrown = member.hasCrown
        model.addAttribute("hasCrown", hasCrown)

        // 내가 오늘 올린 사진 (hasPostedToday = true일 때만 조회)
        if (hasPostedToday) {
            val myPostEntity = postRepository.findAllByPostedDateOrderByCreatedAtDesc(today)
                .firstOrNull { it.member.id == member.id }

            if (myPostEntity != null) {
                val myPost = FeedItem(
                    id = myPostEntity.id?.toInt() ?: 0,
                    nickname = member.nickname,
                    writerEmoji = member.myEmoji,
                    hasCrown = hasCrown,
                    imageUrl = myPostEntity.imageUrl,
                    comment = myPostEntity.content ?: "",
                    createdAt = myPostEntity.createdAt,
                    reactions = mapOf(
                        ReactionType.FIRE to 0,
                        ReactionType.HEART to 0,
                        ReactionType.CLAP to 0,
                        ReactionType.LIKE to 0
                    )  // TODO: 실제 Reaction 데이터로 교체 필요
                )
                model.addAttribute("myPost", myPost)
            }
        } else {
            // 기록 전 상태: 더미 데이터
            val myPost = FeedItem(
                id = 0,
                nickname = member.nickname,
                writerEmoji = member.myEmoji,
                hasCrown = hasCrown,
                imageUrl = "",
                comment = "",
                createdAt = LocalDateTime.now(),
                reactions = emptyMap()
            )
            model.addAttribute("myPost", myPost)
        }

        // ==================== 피드 리스트 (실제 데이터) ====================
        val todayPosts = postRepository.findAllByPostedDateOrderByCreatedAtDesc(today)
        val feedList = todayPosts.map { post ->
            FeedItem(
                id = post.id?.toInt() ?: 0,
                nickname = post.member.nickname,
                writerEmoji = post.member.myEmoji,
                hasCrown = post.member.hasCrown,
                imageUrl = post.imageUrl,
                comment = post.content ?: "",
                createdAt = post.createdAt,
                reactions = mapOf(
                    ReactionType.FIRE to 0,
                    ReactionType.HEART to 0,
                    ReactionType.CLAP to 0,
                    ReactionType.LIKE to 0
                )  // TODO: 실제 Reaction 데이터로 교체 필요
            )
        }
        model.addAttribute("feedList", feedList)
        model.addAttribute("recordCount", feedList.size)

        // ==================== ReactionType Enum 전달 ====================
        // Thymeleaf에서 ReactionType.values()를 순회하기 위해 전달
        model.addAttribute("reactionTypes", ReactionType.entries)

        return "home"
    }

    // 더미 데이터용 DTO
    data class FeedItem(
        val id: Int,
        val nickname: String,
        val writerEmoji: String,
        val hasCrown: Boolean,  // 왕관 보유 여부 (7일 연속 달성자)
        val imageUrl: String,
        val comment: String,
        val createdAt: LocalDateTime,  // 작성 시간
        val reactions: Map<ReactionType, Int>  // ReactionType Enum 사용
    )
}
