package com.kkumo.domain.post

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface PostRepository : JpaRepository<Post, Long> {
    fun existsByMemberIdAndPostedDate(memberId: String, postedDate: LocalDate): Boolean
}
