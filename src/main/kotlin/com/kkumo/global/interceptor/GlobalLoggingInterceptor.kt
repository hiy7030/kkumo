package com.kkumo.global.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class GlobalLoggingInterceptor : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val START_TIME_ATTRIBUTE = "startTime"
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis())
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val startTime = request.getAttribute(START_TIME_ATTRIBUTE) as? Long ?: return
        val endTime = System.currentTimeMillis()
        val latency = endTime - startTime

        val method = request.method
        val uri = request.requestURI
        val status = response.status

        val controllerInfo = if (handler is HandlerMethod) {
            val controllerName = handler.beanType.simpleName
            val methodName = handler.method.name
            " [$controllerName.$methodName]"
        } else {
            ""
        }

        log.info("[$method] $uri ($status) - ${latency}ms$controllerInfo")
    }
}
