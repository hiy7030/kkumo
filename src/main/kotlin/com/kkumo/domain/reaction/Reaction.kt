package com.kkumo.domain.reaction

import com.kkumo.domain.member.Member
import com.kkumo.domain.post.Post
import com.kkumo.global.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "reactions")
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
    @Column(name = "emoji_type", nullable = false)
    var emojiType: ReactionType
) : BaseTimeEntity()
