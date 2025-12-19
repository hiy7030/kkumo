package com.kkumo.domain.reaction.repository

import com.kkumo.domain.member.Member
import com.kkumo.domain.post.Post
import com.kkumo.domain.reaction.Reaction
import com.kkumo.domain.reaction.ReactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReactionRepository : JpaRepository<Reaction, Long> {

    /**
     * 특정 멤버가 특정 게시글들에 남긴 리액션들을 조회 (배치 처리용)
     *
     * @param member 조회할 멤버
     * @param postIds 조회할 게시글 ID 목록
     * @return 기존 리액션 목록
     */
    fun findAllByMemberAndPostIdIn(member: Member, postIds: List<Long>): List<Reaction>

    /**
     * 특정 멤버가 특정 게시글들에 남긴 활성화된 리액션들을 조회 (UI 상태 표시용)
     *
     * @param member 조회할 멤버
     * @param posts 조회할 게시글 목록
     * @return 활성화된 리액션 목록
     */
    fun findAllByMemberAndPostInAndIsActiveTrue(member: Member, posts: List<Post>): List<Reaction>

    /**
     * 특정 게시글들에 대한 리액션 개수를 타입별로 집계 (활성화된 리액션만)
     *
     * @param posts 조회할 게시글 목록
     * @return Map<PostId, Map<ReactionType, Count>>
     */
    @Query("""
        SELECT r.post.id as postId, r.emojiType as reactionType, COUNT(r) as count
        FROM Reaction r
        WHERE r.post IN :posts AND r.isActive = true
        GROUP BY r.post.id, r.emojiType
    """)
    fun countByPostsGroupByType(posts: List<Post>): List<ReactionCountProjection>

    /**
     * 특정 게시글들에 대한 최근 반응자 조회 (타입별로 최대 3명, 최신순, 활성화된 리액션만)
     *
     * @param posts 조회할 게시글 목록
     * @return List<ReactionReactorProjection>
     */
    @Query("""
        SELECT r.post.id as postId,
               r.emojiType as reactionType,
               r.member.nickname as reactorNickname,
               r.createdAt as reactionTime
        FROM Reaction r
        WHERE r.post IN :posts AND r.isActive = true
        ORDER BY r.post.id, r.emojiType, r.createdAt DESC
    """)
    fun findRecentReactorsByPosts(posts: List<Post>): List<ReactionReactorProjection>

    /**
     * ReactionCountProjection 인터페이스
     * JPQL 쿼리 결과를 매핑하기 위한 Projection
     */
    interface ReactionCountProjection {
        fun getPostId(): Long
        fun getReactionType(): ReactionType
        fun getCount(): Long
    }

    /**
     * ReactionReactorProjection 인터페이스
     * 리액션 반응자 정보를 매핑하기 위한 Projection
     */
    interface ReactionReactorProjection {
        fun getPostId(): Long
        fun getReactionType(): ReactionType
        fun getReactorNickname(): String
        fun getReactionTime(): java.time.LocalDateTime
    }
}
