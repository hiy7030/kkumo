package com.kkumo.domain.member.controller

import com.kkumo.domain.member.dto.MailDto
import com.kkumo.domain.member.service.MailService
import com.kkumo.global.annotation.KKumoRestController
import com.kkumo.global.error.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@KKumoRestController
class MailController(
    private val mailService: MailService
) {

    @PostMapping("/mail/send")
    fun sendVerificationCode(
        @Valid @RequestBody request: MailDto.SendRequest
    ): ResponseEntity<ApiResponse<MailDto.MailResponse>> {
        val message = mailService.sendVerificationCode(request.email)
        return ResponseEntity.ok(
            ApiResponse.success(MailDto.MailResponse(message))
        )
    }

    @PostMapping("/mail/verify")
    fun verifyCode(
        @Valid @RequestBody request: MailDto.VerifyRequest
    ): ResponseEntity<ApiResponse<MailDto.MailResponse>> {
        mailService.verifyCode(request.email, request.code)
        return ResponseEntity.ok(
            ApiResponse.success(MailDto.MailResponse("이메일 인증이 완료되었습니다."))
        )
    }
}
