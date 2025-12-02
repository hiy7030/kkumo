package com.kkumo.domain.member

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

object MemberIdGenerator {

    private const val PREFIX = "MID"
    private const val DATE_PATTERN = "yyyyMMdd"
    private const val SHORT_UUID_LENGTH = 7

    fun generate(): String {
        val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN))
        val shortUuid = generateShortUuid()
        return "$PREFIX$dateStr$shortUuid"
    }

    private fun generateShortUuid(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return uuid.substring(0, SHORT_UUID_LENGTH)
    }
}
