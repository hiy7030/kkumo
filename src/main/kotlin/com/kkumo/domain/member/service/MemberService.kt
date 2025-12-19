package com.kkumo.domain.member.service

import com.kkumo.domain.member.Member
import com.kkumo.domain.member.repository.MemberRepository
import com.kkumo.domain.member.dto.MemberDto
import com.kkumo.global.error.BusinessException
import com.kkumo.global.error.ErrorCode
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
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

    @Transactional(readOnly = true)
    fun getMyInfo(
        member: Member,
    ) {

    }

    @Transactional
    fun updateProfile(email: String, request: MemberDto.UpdateRequest) {
        // 1. 왕관 이모지 검증
        val member = memberRepository.findByEmail(email)
            ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)

        validateCrownEmoji(request.myEmoji)

        // 2. 닉네임 중복 검사 (본인 제외)
        memberRepository.findByNickname(request.nickname)?.let { existingMember ->
            if (existingMember.id != member.id) {
                throw BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS)
            }
        }

        // 3. 이모지 중복 검사 (본인 제외)
        memberRepository.findByMyEmoji(request.myEmoji)?.let { existingMember ->
            if (existingMember.id != member.id) {
                throw BusinessException(ErrorCode.EMOJI_ALREADY_EXISTS)
            }
        }

        // 4. 프로필 업데이트 (Dirty Checking)
        member.update(request.nickname, request.myEmoji)
    }
}