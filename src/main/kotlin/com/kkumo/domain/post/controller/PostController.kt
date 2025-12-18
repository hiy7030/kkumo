package com.kkumo.domain.post.controller

import com.kkumo.domain.post.dto.PostDto
import com.kkumo.domain.post.service.PostService
import com.kkumo.global.annotation.KKumoRestController
import com.kkumo.global.auth.CustomUserDetails
import com.kkumo.global.error.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@KKumoRestController
class PostController(
    private val postService: PostService
) {

    /**
     * 게시글 작성 (FormData 방식)
     * POST /kkumo/v1/posts
     *
     * Content-Type: multipart/form-data
     * Parameters:
     * - image: MultipartFile (필수)
     * - content: String? (선택, 최대 140자)
     *
     * Response:
     * { "success": true, "postId": 123 }
     *
     * Note: Spring Security의 @AuthenticationPrincipal을 통해 인증된 사용자 정보를 가져옴
     */
    @PostMapping(
        "/posts",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun createPost(
        @AuthenticationPrincipal user: CustomUserDetails,
        @RequestPart("image") imageFile: MultipartFile,
        @RequestParam("content", required = false) content: String?
    ): PostDto.CreateResponse {
        // CustomUserDetails에서 Member 엔티티 직접 접근
        val member = user.member

        return postService.createPost(member, content, imageFile)
    }

    /**
     * 오늘의 피드 조회
     * GET /kkumo/v1/posts/today
     *
     * 오늘 날짜에 작성된 모든 게시글을 최신순으로 조회
     *
     * Response:
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "id": 1,
     *       "nickname": "강아지러버",
     *       "writerEmoji": "🐶",
     *       "hasCrown": true,
     *       "imageUrl": "https://...",
     *       "content": "...",
     *       "postedDate": "2025-12-16",
     *       "createdAt": "2025-12-16T10:30:00"
     *     }
     *   ]
     * }
     */
    @GetMapping("/posts/today")
    fun getTodayPosts(): ApiResponse<List<PostDto.Response>> {
        val posts = postService.getTodayPosts()
        return ApiResponse.success(posts)
    }

    /**
     * 내 기록 조회
     * GET /kkumo/v1/posts/my
     *
     * 로그인한 사용자의 과거 기록을 페이징하여 조회
     *
     * Query Parameters:
     * - page: 페이지 번호 (0부터 시작, 기본값: 0)
     * - size: 페이지 크기 (기본값: 20)
     *
     * Note: Spring Security의 @AuthenticationPrincipal을 통해 인증된 사용자 정보를 가져옴
     */
    @GetMapping("/posts/my")
    fun getMyPosts(
        @AuthenticationPrincipal user: CustomUserDetails,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ApiResponse<Page<PostDto.Response>> {
        // CustomUserDetails에서 Member 엔티티 직접 접근
        val member = user.member

        val posts = postService.getMyPosts(member, pageable)

        return ApiResponse.success(posts)
    }
}
