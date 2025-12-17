package com.kkumo.domain.reaction.repository

import com.kkumo.domain.post.Post
import com.kkumo.domain.reaction.Reaction
import com.kkumo.domain.reaction.ReactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReactionRepository : JpaRepository<Reaction, Long> {

    /**
     * 특정 게시글들에 대한 리액션 개수를 타입별로 집계
     *
     * @param posts 조회할 게시글 목록
     * @return Map<PostId, Map<ReactionType, Count>>
     */
    @Query("""
        SELECT r.post.id as postId, r.emojiType as reactionType, COUNT(r) as count
        FROM Reaction r
        WHERE r.post IN :posts
        GROUP BY r.post.id, r.emojiType
    """)
    fun countByPostsGroupByType(posts: List<Post>): List<ReactionCountProjection>

    /**
     * ReactionCountProjection 인터페이스
     * JPQL 쿼리 결과를 매핑하기 위한 Projection
     */
    interface ReactionCountProjection {
        fun getPostId(): Long
        fun getReactionType(): ReactionType
        fun getCount(): Long
    }
}
