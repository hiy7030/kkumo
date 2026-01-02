package com.kkumo.domain.reaction.controller

import com.kkumo.domain.reaction.dto.ReactionRequest
import com.kkumo.domain.reaction.service.ReactionService
import com.kkumo.global.annotation.KKumoRestController
import com.kkumo.global.error.ApiResponse
import com.kkumo.global.error.BusinessException
import com.kkumo.global.error.ErrorCode
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@KKumoRestController
class ReactionController(
    private val reactionService: ReactionService
) {

    /**
     * 리액션 일괄 저장 API
     * 화면 이탈 시 클라이언트가 모아둔 리액션들을 한 번에 저장
     *
     * @param userDetails 인증된 사용자 정보
     * @param request 배치 리액션 요청
     * @return 성공 응답
     */
    @PostMapping("/reactions/batch")
    fun saveReactionsBatch(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestBody request: ReactionRequest.Batch
    ): ApiResponse<Unit> {
        println("==================== BATCH REACTION REQUEST ====================")
        println("User: ${userDetails?.username}")
        println("Reactions count: ${request.reactions.size}")
        request.reactions.forEachIndexed { index, reaction ->
            println("  [$index] postId=${reaction.postId}, type=${reaction.reactionType}")
        }
        println("================================================================")

        userDetails?.let {
            reactionService.saveReactionsBatch(it, request)
        }?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        return ApiResponse.success(Unit)

    }
}
