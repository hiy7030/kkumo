package com.kkumo.domain.member.repository

import com.kkumo.domain.member.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, String> {
    fun findByEmail(email: String): Member?
    fun existsByEmail(email: String): Boolean
    fun existsByNickname(nickname: String): Boolean
    fun existsByMyEmoji(emoji: String): Boolean
}