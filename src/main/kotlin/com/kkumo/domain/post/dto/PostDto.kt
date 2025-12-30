package com.kkumo.domain.post.dto

import com.kkumo.domain.post.Post
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 게시글 DTO
 */
object PostDto {

    /**
     * 게시글 작성 응답 DTO (간소화)
     * SSR 환경에서 fetch API로 받기 쉽게 단순화
     */
    data class CreateResponse(
        val success: Boolean,
        val postId: Long? = null,
        val message: String? = null
    ) {
        companion object {
            fun success(postId: Long): CreateResponse {
                return CreateResponse(success = true, postId = postId)
            }

            fun failure(message: String): CreateResponse {
                return CreateResponse(success = false, message = message)
            }
        }
    }

    /**
     * 게시글 조회 응답 DTO
     */
    data class Response(
        val id: Long,
        val nickname: String,
        val writerEmoji: String,
        val hasCrown: Boolean,
        val thumbnailUrl: String,
        val originalImageUrl: String,
        val content: String?,
        val postedDate: LocalDate,
        val createdAt: LocalDateTime,
        val isPrivate: Boolean
    ) {
        companion object {
            fun from(post: Post): Response {
                return Response(
                    id = post.id ?: 0L,
                    nickname = post.member.nickname,
                    writerEmoji = post.member.myEmoji,
                    hasCrown = post.member.hasCrown,
                    thumbnailUrl = post.thumbnailUrl,
                    originalImageUrl = post.originalImageUrl,
                    content = post.content,
                    postedDate = post.postedDate,
                    createdAt = post.createdAt,
                    isPrivate = post.isPrivate
                )
            }
        }
    }
}
