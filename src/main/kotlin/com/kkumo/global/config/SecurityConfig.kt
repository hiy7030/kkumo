package com.kkumo.global.config

import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Spring Security 초기 설정
 * - MVP 개발 편의성을 위한 CSRF disable
 * - 프론트엔드 연동을 위한 CORS 전체 허용
 * - 세션 기반 인증 (formLogin)
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    /**
     * SecurityFilterChain 설정 (Spring Security Kotlin DSL 스타일)
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            // CSRF 비활성화 (MVP 개발 편의성)
            csrf { disable() }

            // CORS 활성화
            cors { }

            // HTTP Basic 비활성화
            httpBasic { disable() }

            // Form Login 활성화 (기본 설정)
            formLogin { }

            // 세션 관리 (기본값: IF_REQUIRED)
            sessionManagement { }

            // 접근 권한 제어
            authorizeHttpRequests {
                // 테스트 API 허용
                authorize("/kkumo/v1/hello", permitAll)

                // 정적 리소스 허용
                authorize(PathRequest.toStaticResources().atCommonLocations(), permitAll)

                // 나머지는 인증 필요
                authorize(anyRequest, authenticated)
            }
        }

        return http.build()
    }

    /**
     * CORS 설정 - 모든 요청 허용 (프론트엔드 연동용)
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
            maxAge = 3600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    /**
     * 비밀번호 암호화를 위한 BCryptPasswordEncoder Bean 등록
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
