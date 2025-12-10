package com.kkumo.domain.member

import com.kkumo.global.common.BaseTimeEntity
import jakarta.persistence.*
import org.springframework.data.domain.Persistable
import java.time.LocalDate

@Entity
@Table(name = "members")
class Member(
    @Id
    @Column(name = "member_id", length = 50)
    val id: String = MemberIdGenerator.generate(),

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var nickname: String,

    @Column(name = "my_emoji", nullable = false, unique = true)
    var myEmoji: String,

    @Column(name = "current_streak", nullable = false)
    var currentStreak: Int = 0,

    @Column(name = "has_crown", nullable = false)
    var hasCrown: Boolean = false,

    @Column(name = "last_posted_at")
    var lastPostedAt: LocalDate? = null
) : BaseTimeEntity()
