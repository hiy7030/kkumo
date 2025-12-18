package com.kkumo.domain.member.controller

import com.kkumo.domain.member.repository.MemberRepository
import com.kkumo.domain.post.service.PostService
import com.kkumo.global.annotation.KKumoWebController
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@KKumoWebController
class MemberViewController(
    private val memberRepository: MemberRepository,
    private val postService: PostService
) {

    @GetMapping("/signup")
    fun signupPage(): String {
        return "signup"
    }

    /**
     * 마이페이지
     * 로그인된 사용자의 정보를 조회하여 화면에 표시
     */
    @GetMapping("/members/me")
    fun myPage(
        @AuthenticationPrincipal userDetails: UserDetails?,
        model: Model
    ): String {
        if (userDetails == null) {
            return "redirect:/login"
        }

        val member = memberRepository.findByEmail(userDetails.username)
            ?: return "redirect:/login"

        model.addAttribute("memberEmail", member.email)
        model.addAttribute("memberNickname", member.nickname)
        model.addAttribute("memberEmoji", member.myEmoji)
        model.addAttribute("currentStreak", member.currentStreak)
        model.addAttribute("hasCrown", member.hasCrown)
        model.addAttribute("hasPostedToday", member.lastPostedAt == java.time.LocalDate.now())

        // 내 기록 목록 조회 (리액션 포함)
        val myFeedList = postService.getMyFeedList(member)
        model.addAttribute("myFeedList", myFeedList)

        return "my-page"
    }
}

@Controller
class LoginViewController {

    @GetMapping("/login")
    fun loginPage(): String {
        return "login"
    }
}
