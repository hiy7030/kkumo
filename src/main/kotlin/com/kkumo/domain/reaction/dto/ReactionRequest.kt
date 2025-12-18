package com.kkumo.domain.reaction.dto

import com.kkumo.domain.reaction.ReactionType

/**
 * 리액션 요청 DTO
 */
object ReactionRequest {

    /**
     * 개별 리액션 요청
     */
    data class Single(
        val postId: Long,
        val reactionType: ReactionType
    )

    /**
     * 배치 리액션 요청 (화면 이탈 시 일괄 전송용)
     */
    data class Batch(
        val reactions: List<Single>
    )
}
