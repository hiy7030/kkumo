package com.kkumo.domain.member.repository

import com.kkumo.domain.member.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface MemberRepository : JpaRepository<Member, String> {
    fun findByEmail(email: String): Member?
    fun existsByEmail(email: String): Boolean
    fun existsByNickname(nickname: String): Boolean
    fun existsByMyEmoji(emoji: String): Boolean

    @Modifying
    @Query("""
        UPDATE Member m
        SET m.currentStreak = 0, m.hasCrown = false
        WHERE m.currentStreak > 0
        AND (m.lastPostedAt != :date OR m.lastPostedAt IS NULL)
    """)
    fun bulkResetStreakNotPostedAt(date: LocalDate): Int
}