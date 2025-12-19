package com.kkumo.domain.post.controller

import com.kkumo.domain.member.repository.MemberRepository
import com.kkumo.domain.post.service.PostService
import com.kkumo.domain.reaction.ReactionType
import com.kkumo.global.annotation.KKumoWebController
import com.kkumo.global.auth.CustomUserDetails
import com.kkumo.global.error.BusinessException
import com.kkumo.global.error.ErrorCode
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@KKumoWebController
class PostViewController(
    private val postService: PostService,
    private val memberRepository: MemberRepository,
    private val kkumoProperties: com.kkumo.global.config.KkumoProperties,
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
    fun homePage(
        @AuthenticationPrincipal user: CustomUserDetails,
        @RequestParam(required = false) date: String?,
        model: Model
    ): String {
        // Base Date 설정
        val baseDate = kkumoProperties.baseDate
        val today = LocalDate.now()
        val requestedDate = date?.let { LocalDate.parse(it) } ?: today

        // Security: baseDate보다 과거 날짜 요청 시 baseDate로 리다이렉트
        if (requestedDate.isBefore(baseDate)) {
            return "redirect:/kkumo/v1/home?date=$baseDate"
        }

        // DB에서 최신 Member 정보를 조회하여 hasPostedToday가 정확히 반영되도록 함
        val currentMember = memberRepository.findById(user.member.id)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        val response = postService.getDailyFeed(
            user = currentMember,
            date = date,
        )

        val prevDate = response.selectedDate.minusDays(1).toString() // yyyy-MM-dd
        val nextDate = response.selectedDate.plusDays(1).toString() // yyyy-MM-dd
        val formattedDate = response.selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd")) // YYMMDD

        // Prev Arrow 활성화 여부: selectedDate가 baseDate보다 이후인 경우에만 활성화
        val isPrevAllowed = response.selectedDate.isAfter(baseDate)

        model.addAttribute("selectedDate", response.selectedDate)
        model.addAttribute("prevDate", prevDate)
        model.addAttribute("nextDate", nextDate)
        model.addAttribute("isToday", response.isToday)
        model.addAttribute("formattedDate", formattedDate)
        model.addAttribute("isPrevAllowed", isPrevAllowed)


        model.addAttribute("hasPostedToday", response.memberInfos.hasPostedToday)
        model.addAttribute("currentStreak", response.memberInfos.currentStreak)
        model.addAttribute("hasCrown", response.memberInfos.hasCrown)


        model.addAttribute("feedList", response.posts)
        model.addAttribute("recordCount", response.posts.size)

        model.addAttribute("reactionTypes", ReactionType.entries)

        return "home"
    }
}
