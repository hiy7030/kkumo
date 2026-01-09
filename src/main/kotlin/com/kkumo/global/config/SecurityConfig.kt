package com.kkumo.global.config

import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import javax.sql.DataSource

/**
 * Spring Security 초기 설정
 * - MVP 개발 편의성을 위한 CSRF disable
 * - 프론트엔드 연동을 위한 CORS 전체 허용
 * - 세션 기반 인증 (formLogin)
 * - DB 기반 Remember-Me (Persistent Token)
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val dataSource: DataSource,
    private val userDetailsService: UserDetailsService
) {

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

            // Form Login 활성화
            formLogin {
                loginPage = "/login"
                defaultSuccessUrl("/kkumo/v1/home", true)
                permitAll()
            }

            // Logout 설정
            logout {
                logoutUrl = "/logout"
                logoutSuccessUrl = "/login?logout"
                invalidateHttpSession = true
                deleteCookies("JSESSIONID", "remember-me")
                permitAll()
            }

            // Remember-Me 설정 (DB 기반 Persistent Token)
            rememberMe {
                key = "kkumo-remember-me-secret-key-2024"
                tokenRepository = persistentTokenRepository()
                userDetailsService = userDetailsService
                tokenValiditySeconds = 1209600 // 14일 (2주)
                rememberMeParameter = "remember-me"
                alwaysRemember = false
            }

            // 세션 관리 (기본값: IF_REQUIRED)
            sessionManagement { }

            // 접근 권한 제어
            authorizeHttpRequests {
                // 인증 페이지 허용
                authorize("/login", permitAll)
                authorize("/kkumo/v1/signup", permitAll)
                authorize("/kkumo/v1/members", permitAll)
                authorize("/kkumo/v1/forgot-password", permitAll)
                authorize("/kkumo/v1/members/reset-password", permitAll)

                // 이메일 인증 허용 (회원가입 시 필요)
                authorize("/kkumo/v1/mail/**", permitAll)
                authorize("/kkumo/v1/test/**", permitAll)

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

    /**
     * Remember-Me 토큰을 DB에 저장하기 위한 PersistentTokenRepository Bean 등록
     * JdbcTokenRepositoryImpl을 사용하여 persistent_logins 테이블에 토큰 저장
     */
    @Bean
    fun persistentTokenRepository(): PersistentTokenRepository {
        return JdbcTokenRepositoryImpl().apply {
            setDataSource(this@SecurityConfig.dataSource)
        }
    }
}
