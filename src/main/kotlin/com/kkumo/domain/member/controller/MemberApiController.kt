package com.kkumo.domain.member.controller

import com.kkumo.domain.member.service.MemberService
import com.kkumo.domain.member.dto.MemberDto
import com.kkumo.domain.member.repository.MemberRepository
import com.kkumo.domain.post.dto.HomeResponse
import com.kkumo.domain.post.service.PostService
import com.kkumo.global.annotation.KKumoRestController
import com.kkumo.global.auth.CustomUserDetails
import com.kkumo.global.error.ApiResponse
import com.kkumo.global.error.BusinessException
import com.kkumo.global.error.ErrorCode
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@KKumoRestController
class MemberApiController(
    private val memberService: MemberService,
    private val postService: PostService,
    private val memberRepository: MemberRepository
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

    /**
     * 내 게시글 목록 조회 (무한 스크롤용)
     * - Slice 기반 페이징 (COUNT 쿼리 없음)
     * - 기본 페이지 크기: 20
     */
    @GetMapping("/members/me/posts")
    fun getMyPosts(
        @AuthenticationPrincipal user: CustomUserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<MyPostsSliceResponse>> {
        val member = memberRepository.findByEmail(user.member.email)
            ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)

        val pageable = PageRequest.of(page, size)
        val feedSlice = postService.getMyFeedList(member, pageable)

        val response = MyPostsSliceResponse(
            content = feedSlice.content,
            hasNext = feedSlice.hasNext(),
            pageNumber = feedSlice.number,
            pageSize = feedSlice.size
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 무한 스크롤 응답 DTO
     */
    data class MyPostsSliceResponse(
        val content: List<HomeResponse.FeedResponse>,
        val hasNext: Boolean,
        val pageNumber: Int,
        val pageSize: Int
    )

    @GetMapping("/test")
    fun test() {
        memberService.test()
    }
}