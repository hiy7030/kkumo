package com.kkumo.domain.post.repository

import com.kkumo.domain.post.Post
import com.kkumo.domain.member.Member
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface PostRepository : JpaRepository<Post, Long> {
    // 1일 1회 작성 제한 체크
    fun existsByMemberAndPostedDate(member: Member, postedDate: LocalDate): Boolean

    // 오늘의 피드 조회 (최신순)
    fun findAllByPostedDateOrderByCreatedAtDesc(postedDate: LocalDate): List<Post>

    // 오늘의 피드 조회 with Member JOIN FETCH (N+1 최적화)
    @Query("""
        SELECT p
        FROM Post p
        JOIN FETCH p.member m
        WHERE p.postedDate = :postedDate
        ORDER BY p.createdAt DESC
    """)
    fun findAllByPostedDateWithMember(@Param("postedDate") postedDate: LocalDate): List<Post>

    // 내 기록 조회 (페이징, 최신순)
    fun findAllByMemberOrderByCreatedAtDesc(member: Member, pageable: Pageable): Page<Post>

    // 내 기록 조회 (Base Date 이후, 페이징, 최신순)
    fun findAllByMemberAndPostedDateGreaterThanEqualOrderByCreatedAtDesc(
        member: Member,
        postedDate: LocalDate,
        pageable: Pageable
    ): Page<Post>

    // 내 기록 조회 (Base Date 이후, Slice, 최신순) - 무한 스크롤용
    fun findSliceByMemberAndPostedDateGreaterThanEqualOrderByCreatedAtDesc(
        member: Member,
        postedDate: LocalDate,
        pageable: Pageable
    ): Slice<Post>

    // 특정 사용자의 특정 기간 게시글 조회 (캘린더용)
    fun findAllByMemberAndPostedDateBetween(member: Member, startDate: LocalDate, endDate: LocalDate): List<Post>

    // 특정 기간의 모든 게시글 조회 (전체 멤버 기록 현황용)
    fun findAllByPostedDateBetween(startDate: LocalDate, endDate: LocalDate): List<Post>
}