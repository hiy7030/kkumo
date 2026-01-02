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
     * 리액션 일괄 저장 (배치 처리 - Toggle 방식)
     * 화면 이탈 시 클라이언트가 모아둔 리액션들을 한 번에 저장
     * 기존 리액션이 있으면 isActive 토글, 없으면 새로 생성
     *
     * @param userDetails 인증된 사용자
     * @param request 배치 리액션 요청
     */
    @Transactional
    fun saveReactionsBatch(userDetails: UserDetails, request: ReactionRequest.Batch) {
        val member = memberRepository.findByEmail(userDetails.username)
            ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)

        if (request.reactions.isEmpty()) {
            return
        }

        // 0. 안전장치: 중복 제거 (postId + reactionType 기준)
        // 프론트에서 압축되었더라도, 혹시 모를 중복 요청에 대비
        val uniqueReactions = request.reactions.distinctBy { Pair(it.postId, it.reactionType) }
        println("[Service] Original: ${request.reactions.size}, After distinct: ${uniqueReactions.size}")

        // 1. 리액션별로 Entity 생성 리스트
        val reactionsToSave = mutableListOf<Reaction>()

        // 2. 각 리액션 요청별로 처리
        uniqueReactions.forEach { req ->
            // 2-1. Post 조회 (존재하지 않으면 스킵)
            val post = postRepository.findById(req.postId).orElse(null) ?: run {
                println("[Service] ⚠️ Post not found: ${req.postId}")
                return@forEach
            }

            // 2-2. 기존 리액션 조회 (member + post + emojiType 정확히 매칭)
            val existingReaction = reactionRepository.findByMemberAndPostAndEmojiType(
                member = member,
                post = post,
                emojiType = req.reactionType
            )

            if (existingReaction != null) {
                // Case A: 기존 리액션 존재 -> isActive만 토글
                println("[Service] Toggle existing: postId=${post.id}, type=${req.reactionType}, ${existingReaction.isActive} -> ${!existingReaction.isActive}")
                existingReaction.isActive = !existingReaction.isActive
                reactionsToSave.add(existingReaction)
            } else {
                // Case B: 기존 리액션 없음 -> 새로 생성 (isActive = true)
                println("[Service] Create new: postId=${post.id}, type=${req.reactionType}")
                val newReaction = Reaction(
                    post = post,
                    member = member,
                    emojiType = req.reactionType,
                    isActive = true
                )
                reactionsToSave.add(newReaction)
            }
        }

        // 3. Entity 전부 일괄 저장
        if (reactionsToSave.isNotEmpty()) {
            println("[Service] Reactions to save: ${reactionsToSave.size}")
            reactionsToSave.forEachIndexed { index, reaction ->
                println("  [$index] postId=${reaction.post.id}, type=${reaction.emojiType}, isActive=${reaction.isActive}")
            }
            reactionRepository.saveAll(reactionsToSave)
            println("[Service] ✅ Batch save completed")
        } else {
            println("[Service] ⚠️ No reactions to save")
        }
    }
}
