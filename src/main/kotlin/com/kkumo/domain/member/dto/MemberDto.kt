package com.kkumo.domain.member.dto

import com.kkumo.domain.member.Member
import com.kkumo.global.validation.PasswordMatch
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

object MemberDto {

    @PasswordMatch
    data class SignupRequest(
        @field:NotBlank(message = "이메일은 필수입니다.")
        @field:Email(message = "올바른 이메일 형식이 아닙니다.")
        val email: String,

        @field:NotBlank(message = "비밀번호는 필수입니다.")
        @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        val password: String,

        @field:NotBlank(message = "비밀번호 확인은 필수입니다.")
        val passwordConfirm: String,

        @field:NotBlank(message = "닉네임은 필수입니다.")
        @field:Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
        val nickname: String,

        @field:NotBlank(message = "이모지를 선택해주세요.")
        val myEmoji: String
    ) {
        fun toEntity(encodedPassword: String): Member {
            return Member(
                email = email,
                password = encodedPassword,
                nickname = nickname,
                myEmoji = myEmoji
            )
        }
    }

    data class SignupResponse(
        val mid: String,
        val email: String,
        val nickname: String,
        val myEmoji: String
    ) {
        companion object {
            fun from(member: Member): SignupResponse {
                return SignupResponse(
                    mid = member.id,
                    email = member.email,
                    nickname = member.nickname,
                    myEmoji = member.myEmoji
                )
            }
        }
    }

    data class UpdateRequest(
        @field:NotBlank(message = "닉네임은 필수입니다.")
        @field:Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
        val nickname: String,

        @field:NotBlank(message = "이모지를 선택해주세요.")
        val myEmoji: String
    )

    @PasswordMatch(passwordField = "newPassword", passwordConfirmField = "newPasswordConfirm")
    data class PasswordResetRequest(
        @field:NotBlank(message = "이메일은 필수입니다.")
        @field:Email(message = "올바른 이메일 형식이 아닙니다.")
        val email: String,

        @field:NotBlank(message = "인증번호는 필수입니다.")
        @field:Size(min = 6, max = 6, message = "인증번호는 6자리여야 합니다.")
        val code: String,

        @field:NotBlank(message = "새 비밀번호는 필수입니다.")
        @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        val newPassword: String,

        @field:NotBlank(message = "비밀번호 확인은 필수입니다.")
        val newPasswordConfirm: String
    )
}
