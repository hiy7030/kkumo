package com.kkumo.domain.member.controller

import com.kkumo.global.annotation.KKumoWebController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@KKumoWebController
class MemberViewController {

    @GetMapping("/signup")
    fun signupPage(): String {
        return "signup"
    }
}

@Controller
class LoginViewController {

    @GetMapping("/login")
    fun loginPage(): String {
        return "login"
    }
}
