package com.kkumo.domain.post

import com.kkumo.domain.member.Member
import com.kkumo.global.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "posts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_member_date",
            columnNames = ["member_id", "posted_date"]
        )
    ]
)
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = true, columnDefinition = "TEXT", length = 140)
    var content: String? = null,

    @Column(name = "image_url", nullable = false)
    var imageUrl: String,

    @Column(name = "posted_date", nullable = false)
    val postedDate: LocalDate
) : BaseTimeEntity()
