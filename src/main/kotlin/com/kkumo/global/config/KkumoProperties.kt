package com.kkumo.global.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * KKUMO 애플리케이션 전역 설정 Properties
 *
 * application.yml의 `kkumo.*` 설정을 바인딩
 */
@Component
@ConfigurationProperties(prefix = "kkumo")
class KkumoProperties {
    /**
     * 기준일 (Base Date)
     *
     * 이 날짜 이전의 데이터 조회 및 네비게이션을 제한
     * (당일은 접근 가능)
     */
    var baseDate: LocalDate = LocalDate.of(2025, 12, 1)
}
