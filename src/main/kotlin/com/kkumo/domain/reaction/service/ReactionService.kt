package com.kkumo.domain.reaction.service

import com.kkumo.domain.member.repository.MemberRepository
import com.kkumo.domain.post.repository.PostRepository
import com.kkumo.domain.reaction.Reaction
import com.kkumo.domain.reaction.dto.ReactionRequest
import com.kkumo.domain.reaction.repository.ReactionRepository
import com.kkumo.global.error.BusinessException
import com.kkumo.global.error.ErrorCode
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReactionService(
    private val reactionRepository: ReactionRepository,
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository
) {

    /**
     * 리액션 일괄 저장 (배치 처리)
     * 화면 이탈 시 클라이언트가 모아둔 리액션들을 한 번에 저장
     *
     * @param member 인증된 사용자
     * @param request 배치 리액션 요청
     */
    @Transactional
    fun saveReactionsBatch(userDetails: UserDetails, request: ReactionRequest.Batch) {
        val member = memberRepository.findByEmail(userDetails.username)
            ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)

        if (request.reactions.isEmpty()) {
            return
        }

        // Post ID 목록 추출
        val postIds = request.reactions.map { it.postId }.distinct()

        // 모든 게시글 조회 (존재 여부 검증)
        val posts = postRepository.findAllById(postIds)
        val postMap = posts.associateBy { it.id }

        // 유효한 리액션만 필터링하여 저장
        val reactions = request.reactions.mapNotNull { req ->
            val post = postMap[req.postId] ?: return@mapNotNull null

            Reaction(
                post = post,
                member = member,
                emojiType = req.reactionType
            )
        }

        // 일괄 저장
        reactionRepository.saveAll(reactions)
    }
}
