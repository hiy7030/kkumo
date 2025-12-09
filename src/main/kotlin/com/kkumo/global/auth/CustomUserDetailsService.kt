package com.kkumo.global.auth

import com.kkumo.domain.member.MemberRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val memberRepository: MemberRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val member = memberRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found with email: $email")

        return CustomUserDetails(member)
    }
}
