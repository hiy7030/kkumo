package com.kkumo.domain.reaction

import com.kkumo.domain.member.Member
import com.kkumo.domain.post.Post
import com.kkumo.global.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "reactions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_reaction_member_post_emoji",
            columnNames = ["member_id", "post_id", "emoji_type"]
        )
    ]
)
class Reaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Enumerated(EnumType.STRING)
    @Column(name = "emoji_type", nullable = false, length = 20)
    var emojiType: ReactionType,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
) : BaseTimeEntity()
