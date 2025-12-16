package com.kkumo.global.auth

import com.kkumo.domain.member.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    val member: Member  // public으로 변경하여 user.member로 직접 접근 가능
) : UserDetails {

    override fun getUsername(): String = member.email

    override fun getPassword(): String = member.password

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_USER"))

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    // Convenience method: member.id를 직접 가져오기
    fun getMemberId(): String = member.id
}
