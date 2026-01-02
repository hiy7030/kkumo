package com.kkumo.domain.reaction.repository

import com.kkumo.domain.member.Member
import com.kkumo.domain.post.Post
import com.kkumo.domain.reaction.Reaction
import com.kkumo.domain.reaction.ReactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReactionRepository : JpaRepository<Reaction, Long> {

    /**
     * 특정 멤버가 특정 게시글에 특정 타입의 리액션을 조회
     *
     * @param member 조회할 멤버
     * @param post 조회할 게시글
     * @param emojiType 리액션 타입
     * @return 기존 리액션 (없으면 null)
     */
    fun findByMemberAndPostAndEmojiType(member: Member, post: Post, emojiType: ReactionType): Reaction?

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
     * N+1 방지를 위해 필요한 필드(post_id, emoji_type)만 Projection으로 조회
     *
     * @param memberId 조회할 멤버 ID
     * @param postIds 조회할 게시글 ID 목록
     * @return 활성화된 리액션 Projection 목록
     */
    @Query(value = """
        SELECT r.post_id as postId, r.emoji_type as emojiType
        FROM reactions r
        WHERE r.member_id = :memberId
          AND r.post_id IN :postIds
          AND r.is_active = true
    """, nativeQuery = true)
    fun findMyActiveReactions(memberId: String, postIds: List<Long>): List<MyActiveReactionProjection>

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
     * Window Function을 사용하여 DB 레벨에서 TOP 3만 추출
     *
     * @param postIds 조회할 게시글 ID 목록
     * @return List<ReactionReactorProjection>
     */
    @Query(value = """
        WITH ranked_reactions AS (
            SELECT
                r.post_id AS postId,
                r.emoji_type AS reactionType,
                m.nickname AS reactorNickname,
                r.created_at AS reactionTime,
                ROW_NUMBER() OVER (
                    PARTITION BY r.post_id, r.emoji_type
                    ORDER BY r.created_at DESC
                ) AS rn
            FROM reactions r
            INNER JOIN members m ON r.member_id = m.member_id
            WHERE r.post_id IN :postIds AND r.is_active = true
        )
        SELECT postId, reactionType, reactorNickname, reactionTime
        FROM ranked_reactions
        WHERE rn <= 3
        ORDER BY postId, reactionType, reactionTime DESC
    """, nativeQuery = true)
    fun findRecentReactorsByPosts(postIds: List<Long>): List<ReactionReactorProjection>

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

    /**
     * MyActiveReactionProjection 인터페이스
     * 내가 남긴 활성화된 리액션 정보를 매핑하기 위한 Projection (N+1 방지용)
     */
    interface MyActiveReactionProjection {
        fun getPostId(): Long
        fun getEmojiType(): ReactionType
    }
}
