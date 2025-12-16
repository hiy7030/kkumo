package com.kkumo.domain.post.controller

import com.kkumo.global.annotation.KKumoWebController
import com.kkumo.global.auth.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@KKumoWebController
class PostViewController {

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
}
