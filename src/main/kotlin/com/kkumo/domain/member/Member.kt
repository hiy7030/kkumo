package com.kkumo.domain.member

import com.kkumo.domain.member.dto.MemberIdGenerator
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
) : BaseTimeEntity() {

    /**
     * 게시글 작성 성공 시 호출되는 비즈니스 로직
     * Streak 증가 및 Crown 부여를 담당
     *
     * @param postedDate 게시글 작성 날짜 (일반적으로 오늘)
     */
    fun succeedPost(postedDate: LocalDate) {
        // 1. Streak 증가 로직 (방어적 설계)
        val yesterday = postedDate.minusDays(1)

        currentStreak = when {
            // 어제 작성했다면 -> 연속 작성이므로 +1
            lastPostedAt == yesterday -> currentStreak + 1
            // 어제보다 과거이거나 null이라면 -> 새로운 시작 (스케줄러가 초기화 했겠지만 방어)
            else -> 1
        }

        // 2. Crown (왕관) 부여
        if (currentStreak >= 7) {
            hasCrown = true
        }

        // 3. 마지막 작성 날짜 갱신
        lastPostedAt = postedDate
    }

    /**
     * 프로필 정보 업데이트 (닉네임, 이모지)
     * JPA Dirty Checking을 통해 자동으로 DB에 반영됨
     *
     * @param nickname 변경할 닉네임
     * @param myEmoji 변경할 이모지
     */
    fun update(nickname: String, myEmoji: String) {
        this.nickname = nickname
        this.myEmoji = myEmoji
    }
}
