package com.kkumo.global.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.full.memberProperties

class PasswordMatchValidator : ConstraintValidator<PasswordMatch, Any> {

    private lateinit var passwordField: String
    private lateinit var passwordConfirmField: String
    private lateinit var message: String

    override fun initialize(constraintAnnotation: PasswordMatch) {
        this.passwordField = constraintAnnotation.passwordField
        this.passwordConfirmField = constraintAnnotation.passwordConfirmField
        this.message = constraintAnnotation.message
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }

        try {
            val kClass = value::class
            val passwordProperty = kClass.memberProperties.find { it.name == passwordField }
            val passwordConfirmProperty = kClass.memberProperties.find { it.name == passwordConfirmField }

            val password = passwordProperty?.call(value) as? String
            val passwordConfirm = passwordConfirmProperty?.call(value) as? String

            val isValid = password == passwordConfirm

            if (!isValid) {
                // 기본 메시지 비활성화
                context.disableDefaultConstraintViolation()
                // passwordConfirm 필드에 에러 메시지 바인딩
                context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(passwordConfirmField)
                    .addConstraintViolation()
            }

            return isValid
        } catch (e: Exception) {
            return false
        }
    }
}
