package com.kkumo.domain.member.dto

import com.kkumo.domain.member.Member
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

object MemberDto {

    data class SignupRequest(
        @field:NotBlank(message = "이메일은 필수입니다.")
        @field:Email(message = "올바른 이메일 형식이 아닙니다.")
        val email: String,

        @field:NotBlank(message = "비밀번호는 필수입니다.")
        @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        val password: String,

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
}
