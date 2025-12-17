package com.kkumo.domain.post.controller

import com.kkumo.domain.post.service.PostService
import com.kkumo.domain.reaction.ReactionType
import com.kkumo.global.annotation.KKumoWebController
import com.kkumo.global.auth.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@KKumoWebController
class PostViewController(
    private val postService: PostService,
) {

    /**
     * 게시글 작성 페이지
     * GET /kkumo/v1/post/create
     *
     * 트위터(X) 스타일의 심플한 작성 화면
     */
    @GetMapping("/post/create")
    fun createPostPage(
        @AuthenticationPrincipal user: CustomUserDetails,
        model: Model
    ): String {
        val member = user.member

        // 사용자 정보를 모델에 추가
        model.addAttribute("nickname", member.nickname)
        model.addAttribute("emoji", member.myEmoji)

        return "create-post"
    }

    @GetMapping("/home")
    fun homePage2(
        @AuthenticationPrincipal user: CustomUserDetails,
        @RequestParam(required = false) date: String?,
        model: Model
    ): String {

        val response = postService.getDailyFeed(
            user = user.member,
            date = date,
        )

        val prevDate = response.selectedDate.minusDays(1).toString() // yyyy-MM-dd
        val nextDate = response.selectedDate.plusDays(1).toString() // yyyy-MM-dd
        val formattedDate = response.selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd")) // YYMMDD

        model.addAttribute("selectedDate", response.selectedDate)
        model.addAttribute("prevDate", prevDate)
        model.addAttribute("nextDate", nextDate)
        model.addAttribute("isToday", response.isToday)
        model.addAttribute("formattedDate", formattedDate)


        model.addAttribute("hasPostedToday", response.memberInfos.hasPostedToday)
        model.addAttribute("currentStreak", response.memberInfos.currentStreak)
        model.addAttribute("hasCrown", response.memberInfos.hasCrown)


        model.addAttribute("feedList", response.posts)
        model.addAttribute("recordCount", response.posts.size)

        model.addAttribute("reactionTypes", ReactionType.entries)

        return "home"
    }
}
