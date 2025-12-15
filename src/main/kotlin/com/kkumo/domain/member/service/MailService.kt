package com.kkumo.domain.member.service

import com.kkumo.global.error.BusinessException
import com.kkumo.global.error.ErrorCode
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Service
class MailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) {

    private val verificationStorage = ConcurrentHashMap<String, VerificationInfo>()

    data class VerificationInfo(
        val code: String,
        val expiresAt: LocalDateTime
    )

    fun sendVerificationCode(email: String): String {
        val code = generateVerificationCode()
        val expiresAt = LocalDateTime.now().plusMinutes(3)

        verificationStorage[email] = VerificationInfo(code, expiresAt)

        sendMail(email, code)

        return "인증번호가 이메일로 전송되었습니다."
    }

    fun verifyCode(email: String, code: String): Boolean {
        val info = verificationStorage[email]
            ?: throw BusinessException(ErrorCode.VERIFICATION_CODE_NOT_FOUND)

        if (LocalDateTime.now().isAfter(info.expiresAt)) {
            verificationStorage.remove(email)
            throw BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED)
        }

        if (info.code != code) {
            throw BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH)
        }

        verificationStorage.remove(email)
        return true
    }

    private fun generateVerificationCode(): String {
        return String.format("%06d", Random.nextInt(0, 1000000))
    }

    private fun sendMail(email: String, code: String) {
        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(email)
            helper.setSubject("👑 KKUMO 이메일 인증번호")
            helper.setText(buildEmailContent(code), true)

            mailSender.send(message)
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.EMAIL_SEND_FAILED)
        }
    }

    private fun buildEmailContent(code: String): String {
        val context = Context()
        context.setVariable("code", code)
        return templateEngine.process("mail/verification-email", context)
    }
}
