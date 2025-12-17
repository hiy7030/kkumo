package com.kkumo.domain.post.service

import com.kkumo.domain.image.service.ImageService
import com.kkumo.domain.member.Member
import com.kkumo.domain.member.repository.MemberRepository
import com.kkumo.domain.post.Post
import com.kkumo.domain.post.dto.HomeResponse
import com.kkumo.domain.post.dto.PostCreateResponse
import com.kkumo.domain.post.dto.PostResponse
import com.kkumo.domain.post.repository.PostRepository
import com.kkumo.domain.reaction.ReactionType
import com.kkumo.domain.reaction.repository.ReactionRepository
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
    private val memberRepository: MemberRepository,
    private val imageService: ImageService,
    private val reactionRepository: ReactionRepository
) {

    /**
     * 게시글 작성
     * - 1일 1회 제한 체크
     * - 이미지 업로드
     * - Post 저장
     * - Member의 Streak 및 Crown 업데이트
     */
    @Transactional
    fun createPost(
        member: Member,
        content: String?,
        imageFile: MultipartFile
    ): PostCreateResponse {
        val today = LocalDate.now()

        // 1. 1일 1회 작성 제한 체크
        if (postRepository.existsByMemberAndPostedDate(member, today)) {
            throw BusinessException(ErrorCode.POST_ALREADY_EXISTS_TODAY)
        }

        // 2. 이미지 업로드 (썸네일 + 원본)
        val uploadResult = imageService.upload(imageFile)

        // 3. Post 엔티티 생성 및 저장
        val post = Post(
            member = member,
            content = content?.take(140), // 최대 140자 제한
            thumbnailUrl = uploadResult.thumbnailUrl,
            originalImageUrl = uploadResult.originalImageUrl,
            postedDate = today  // 서버 기준 날짜 사용
        )

        val savedPost = postRepository.save(post)

        // 4. Member 업데이트 (Streak & Crown)
        // 영속 상태의 Member를 조회하여 Dirty Checking 활성화
        val persistentMember = memberRepository.findById(member.id)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        // 도메인 로직 호출 -> JPA Dirty Checking으로 자동 DB 반영
        persistentMember.succeedPost(today)

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

    /**
     * 특정 날짜의 피드 조회 (홈 화면용)
     * - 선택된 날짜에 작성된 모든 게시글
     * - 실제 Reaction 개수 집계
     * - 최신순 정렬
     *
     * @param date 조회할 날짜
     * @return FeedResponse 리스트
     */
    fun getDailyFeed(
        user: Member,
        date: String?
    ): HomeResponse.HomeResponse {
        val today = LocalDate.now()
        val selectedDate = date?.let { LocalDate.parse(it) } ?: today
        val isToday = selectedDate == today

        // 1. 해당 날짜의 게시글 조회
        val posts = postRepository.findAllByPostedDateOrderByCreatedAtDesc(selectedDate)

        // 2. 모든 게시글의 리액션 개수 집계
        val reactionCounts = reactionRepository.countByPostsGroupByType(posts)

        // 3. Post ID별로 ReactionType -> Count 맵 생성
        val reactionMap = reactionCounts
            .groupBy { it.getPostId() }
            .mapValues { (_, projections) ->
                projections.associate { it.getReactionType() to it.getCount().toInt() }
            }

        // 4. Post 엔티티를 FeedResponse로 변환
        val dailyPosts = posts.map { post ->
            val postReactions = reactionMap[post.id] ?: emptyMap()

            // 모든 ReactionType에 대해 개수 초기화 (없는 타입은 0)
            val allReactions = ReactionType.entries.associateWith { type ->
                postReactions[type] ?: 0
            }

            HomeResponse.FeedResponse.from(post, allReactions)
        }



        return HomeResponse.HomeResponse(
            memberInfos = HomeResponse.MemberResponse(
                currentStreak = user.currentStreak,
                hasCrown = user.hasCrown,
                hasPostedToday = user.lastPostedAt?.let { it == today }?: false,
            ),
            isToday = isToday,
            selectedDate = selectedDate,
            posts = dailyPosts,
        )
    }
}
