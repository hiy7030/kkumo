package com.kkumo.global.error

import com.kkumo.global.annotation.KKumoRestController
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(annotations = [KKumoRestController::class])
class ApiExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        log.error("BusinessException occurred: ${ex.errorCode} - ${ex.message}", ex)

        val response = ApiResponse.error<Unit>(ex.errorCode, ex.message)
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        log.error("Validation error occurred", ex)

        val errorMessage = ex.bindingResult.allErrors
            .joinToString(", ") { it.defaultMessage ?: "Invalid input" }

        val response = ApiResponse.error<Unit>(ErrorCode.INVALID_INPUT, errorMessage)
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
        log.error("Unexpected error occurred", ex)

        val response = ApiResponse.error<Unit>(ErrorCode.INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
