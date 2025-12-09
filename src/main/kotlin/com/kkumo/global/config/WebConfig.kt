package com.kkumo.global.config

import com.kkumo.global.interceptor.GlobalLoggingInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val globalLoggingInterceptor: GlobalLoggingInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(globalLoggingInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/css/**",
                "/js/**",
                "/images/**",
                "/static/**",
                "/favicon.ico",
                "/error"
            )
    }
}
