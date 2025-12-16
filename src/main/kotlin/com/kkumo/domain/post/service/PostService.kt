package com.kkumo.domain.post.service

import com.kkumo.domain.image.service.ImageService
import com.kkumo.domain.member.Member
import com.kkumo.domain.post.Post
import com.kkumo.domain.post.dto.PostCreateResponse
import com.kkumo.domain.post.dto.PostResponse
import com.kkumo.domain.post.repository.PostRepository
import com.kkumo.global.error.BusinessException
import com.kkumo.global.error.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository,
    private val imageService: ImageService
) {

    /**
     * 게시글 작성
     * - 1일 1회 제한 체크
     * - 이미지 업로드
     * - Post 저장
     */
    @Transactional
    fun createPost(
        member: Member,
        content: String?,
        imageFile: MultipartFile
    ): PostCreateResponse {
        val today = LocalDate.now()

        // 1일 1회 작성 제한 체크
        if (postRepository.existsByMemberAndPostedDate(member, today)) {
            throw BusinessException(ErrorCode.POST_ALREADY_EXISTS_TODAY)
        }

        // 이미지 업로드
        val imageUrl = imageService.upload(imageFile)

        // Post 엔티티 생성 및 저장
        val post = Post(
            member = member,
            content = content?.take(140), // 최대 140자 제한
            imageUrl = imageUrl,
            postedDate = today  // 서버 기준 날짜 사용
        )

        val savedPost = postRepository.save(post)

        return PostCreateResponse.success(savedPost.id ?: 0L)
    }

    /**
     * 오늘의 피드 조회
     * - 오늘 날짜에 작성된 모든 게시글
     * - 최신순 정렬
     */
    fun getTodayPosts(): List<PostResponse> {
        val today = LocalDate.now()
        val posts = postRepository.findAllByPostedDateOrderByCreatedAtDesc(today)
        return posts.map { PostResponse.from(it) }
    }

    /**
     * 내 기록 조회
     * - 로그인한 사용자의 과거 기록
     * - 페이징 처리
     * - 최신순 정렬
     */
    fun getMyPosts(member: Member, pageable: Pageable): Page<PostResponse> {
        val posts = postRepository.findAllByMemberOrderByCreatedAtDesc(member, pageable)
        return posts.map { PostResponse.from(it) }
    }
}
