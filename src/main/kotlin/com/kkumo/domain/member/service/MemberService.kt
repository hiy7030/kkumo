package com.kkumo.domain.member.service

import com.kkumo.domain.member.MemberRepository
import com.kkumo.domain.member.dto.MemberDto
import com.kkumo.global.error.BusinessException
import com.kkumo.global.error.ErrorCode
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun signup(request: MemberDto.SignupRequest): MemberDto.SignupResponse {
        validateDuplicateMember(request)
        validateCrownEmoji(request.myEmoji)

        val encodedPassword = passwordEncoder.encode(request.password)
        val member = request.toEntity(encodedPassword)
        val savedMember = memberRepository.save(member)

        return MemberDto.SignupResponse.from(savedMember)
    }

    private fun validateDuplicateMember(request: MemberDto.SignupRequest) {
        if (memberRepository.existsByEmail(request.email)) {
            throw BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS)
        }
        if (memberRepository.existsByNickname(request.nickname)) {
            throw BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS)
        }
        if (memberRepository.existsByMyEmoji(request.myEmoji)) {
            throw BusinessException(ErrorCode.EMOJI_ALREADY_EXISTS)
        }
    }

    private fun validateCrownEmoji(emoji: String) {
        if (emoji == "👑") {
            throw BusinessException(ErrorCode.CROWN_EMOJI_NOT_ALLOWED)
        }
    }
}