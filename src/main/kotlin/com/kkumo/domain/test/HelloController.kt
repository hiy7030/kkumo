package com.kkumo.domain.test

import com.kkumo.global.auth.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HelloController {

    @GetMapping("/")
    fun hello(
        @AuthenticationPrincipal user: CustomUserDetails?,
    ): String {
        if(user != null && user.getMemberId() != null) {
            return "redirect:/kkumo/v1/home"
        }

        return "redirect:/login"
    }
}
