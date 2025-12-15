package com.kkumo.domain.member.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

object MailDto {

    data class SendRequest(
        @field:NotBlank(message = "이메일은 필수입니다.")
        @field:Email(message = "올바른 이메일 형식이 아닙니다.")
        val email: String
    )

    data class VerifyRequest(
        @field:NotBlank(message = "이메일은 필수입니다.")
        @field:Email(message = "올바른 이메일 형식이 아닙니다.")
        val email: String,

        @field:NotBlank(message = "인증번호는 필수입니다.")
        @field:Size(min = 6, max = 6, message = "인증번호는 6자리여야 합니다.")
        val code: String
    )

    data class MailResponse(
        val message: String
    )
}
