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

        // 1. Post ID 목록 추출
        val postIds = uniqueReactions.map { it.postId }.distinct()

        // 2. 모든 게시글 조회 (존재 여부 검증)
        val posts = postRepository.findAllById(postIds)
        val postMap = posts.associateBy { it.id }

        // 3. 기존 리액션 조회 및 Map으로 변환 (postId + emojiType을 키로 사용)
        val existingReactions = reactionRepository.findAllByMemberAndPostIdIn(member, postIds)
        val existingReactionMap = existingReactions.associateBy {
            "${it.post.id}_${it.emojiType}"
        }

        // 4. 토글 로직 수행
        val reactionsToSave = mutableListOf<Reaction>()

        uniqueReactions.forEach { req ->
            val post = postMap[req.postId] ?: return@forEach
            val key = "${req.postId}_${req.reactionType}"
            val existingReaction = existingReactionMap[key]

            if (existingReaction != null) {
                // Case A: 기존 리액션 존재 -> isActive 반전 (Toggle)
                existingReaction.isActive = !existingReaction.isActive
                reactionsToSave.add(existingReaction)
            } else {
                // Case B: 기존 리액션 없음 -> 새로 생성 (isActive = true)
                val newReaction = Reaction(
                    post = post,
                    member = member,
                    emojiType = req.reactionType,
                    isActive = true
                )
                reactionsToSave.add(newReaction)
            }
        }

        // 5. 일괄 저장
        reactionRepository.saveAll(reactionsToSave)
    }
}
