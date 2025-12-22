package com.kkumo.domain.member.controller

import com.kkumo.domain.member.service.MemberService
import com.kkumo.domain.member.dto.MemberDto
import com.kkumo.global.annotation.KKumoRestController
import com.kkumo.global.auth.CustomUserDetails
import com.kkumo.global.error.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@KKumoRestController
class MemberApiController(
    private val memberService: MemberService
) {

    @PostMapping("/members")
    fun signup(@Valid @RequestBody request: MemberDto.SignupRequest): ResponseEntity<ApiResponse<MemberDto.SignupResponse>> {
        val response = memberService.signup(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    @PatchMapping("/members")
    fun updateProfile(
        @AuthenticationPrincipal user: CustomUserDetails,
        @Valid @RequestBody request: MemberDto.UpdateRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val email = user.member.email
        memberService.updateProfile(email, request)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    @PostMapping("/members/reset-password")
    fun resetPassword(
        @Valid @RequestBody request: MemberDto.PasswordResetRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        memberService.resetPassword(request)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }
}