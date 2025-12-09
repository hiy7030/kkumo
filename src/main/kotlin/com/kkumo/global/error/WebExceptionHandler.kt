package com.kkumo.global.error

import com.kkumo.global.annotation.KKumoWebController
import org.slf4j.LoggerFactory
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice(annotations = [KKumoWebController::class])
class WebExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException, model: Model): String {
        log.error("BusinessException occurred: ${ex.errorCode} - ${ex.message}", ex)

        model.addAttribute("message", ex.message)
        model.addAttribute("nextUrl", null)
        return "common/alert"
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex: Exception, model: Model): String {
        log.error("Unexpected error occurred", ex)

        model.addAttribute("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.name)
        model.addAttribute("errorMessage", ErrorCode.INTERNAL_SERVER_ERROR.message)
        return "error/errorPage"
    }
}
