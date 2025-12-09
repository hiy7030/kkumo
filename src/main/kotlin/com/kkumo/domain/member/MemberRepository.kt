package com.kkumo.domain.member

import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, String> {
    fun findByEmail(email: String): Member?
    fun existsByEmail(email: String): Boolean
    fun existsByNickname(nickname: String): Boolean
    fun existsByMyEmoji(emoji: String): Boolean
}
