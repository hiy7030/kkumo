package com.kkumo.domain.test

import com.kkumo.global.annotation.KKumoRestController
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDateTime

/**
 * Spring Security 설정 확인용 테스트 컨트롤러
 * 실제 URL: GET /kkumo/v1/hello
 */
@KKumoRestController
class HelloController {

    /**
     * 기본 헬스체크 엔드포인트 (인증 불필요)
     * SecurityConfig에서 permitAll로 설정됨
     */
    @GetMapping("/hello")
    fun hello(): Map<String, Any> {
        return mapOf(
            "message" to "Hello from KKUMO! 🌟",
            "timestamp" to LocalDateTime.now(),
            "status" to "OK",
            "info" to "이 엔드포인트는 인증 없이 접근 가능합니다."
        )
    }

    /**
     * 인증이 필요한 테스트 엔드포인트
     * SecurityConfig에서 authenticated로 보호됨
     */
    @GetMapping("/secured")
    fun secured(): Map<String, Any> {
        return mapOf(
            "message" to "인증된 사용자만 볼 수 있는 데이터입니다.",
            "timestamp" to LocalDateTime.now(),
            "status" to "AUTHENTICATED"
        )
    }
}
