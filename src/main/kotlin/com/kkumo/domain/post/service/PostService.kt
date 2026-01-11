package com.kkumo.domain.post.service

import com.kkumo.domain.image.service.ImageService
import com.kkumo.domain.member.Member
import com.kkumo.domain.member.repository.MemberRepository
import com.kkumo.domain.post.Post
import com.kkumo.domain.post.dto.HomeResponse
import com.kkumo.domain.post.dto.PostDto
import com.kkumo.domain.post.repository.PostRepository
import com.kkumo.domain.reaction.ReactionType
import com.kkumo.domain.reaction.repository.ReactionRepository
import com.kkumo.global.error.BusinessException
import com.kkumo.global.error.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
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
    private val reactionRepository: ReactionRepository,
    private val kkumoProperties: com.kkumo.global.config.KkumoProperties,
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
        imageFile: MultipartFile,
        isPrivate: Boolean = false
    ): PostDto.CreateResponse {
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
            postedDate = today,  // 서버 기준 날짜 사용
            isPrivate = isPrivate
        )

        val savedPost = postRepository.save(post)

        // 4. Member 업데이트 (Streak & Crown)
        // getReferenceById를 사용하여 불필요한 SELECT 쿼리 제거 (프록시 객체 반환)
        val persistentMember = memberRepository.getReferenceById(member.id)

        // 도메인 로직 호출 -> JPA Dirty Checking으로 자동 DB 반영
        persistentMember.succeedPost(today)

        return PostDto.CreateResponse.success(savedPost.id ?: 0L)
    }

    /**
     * 오늘의 피드 조회
     * - 오늘 날짜에 작성된 모든 게시글
     * - 최신순 정렬
     */
    fun getTodayPosts(): List<PostDto.Response> {
        val today = LocalDate.now()
        val posts = postRepository.findAllByPostedDateOrderByCreatedAtDesc(today)
        return posts.map { PostDto.Response.from(it) }
    }

    /**
     * 내 기록 조회
     * - 로그인한 사용자의 과거 기록
     * - 페이징 처리
     * - 최신순 정렬
     */
    fun getMyPosts(member: Member, pageable: Pageable): Page<PostDto.Response> {
        val posts = postRepository.findAllByMemberOrderByCreatedAtDesc(member, pageable)
        return posts.map { PostDto.Response.from(it) }
    }

    /**
     * 특정 날짜의 피드 조회 (홈 화면용)
     * - 선택된 날짜에 작성된 모든 게시글
     * - 실제 Reaction 개수 집계
     * - 최신순 정렬
     * - N+1 문제 방지를 위해 Member JOIN FETCH 적용
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

        // 1. 해당 날짜의 게시글 조회 (Member JOIN FETCH로 N+1 방지)
        val posts = postRepository.findAllByPostedDateWithMember(selectedDate)

        // 2. 모든 게시글의 리액션 개수 집계
        val reactionCounts = reactionRepository.countByPostsGroupByType(posts)

        // 3. 모든 게시글의 최근 반응자 조회 (Window Function으로 DB 레벨에서 TOP 3 추출)
        val postIds = posts.mapNotNull { it.id }
        val reactionReactors = reactionRepository.findRecentReactorsByPosts(postIds)

        // 4. 현재 사용자가 남긴 활성화된 리액션 조회 (N+1 방지: Projection 사용)
        val myActiveReactions = reactionRepository.findMyActiveReactions(user.id, postIds)
        val myReactionMap = myActiveReactions
            .groupBy { it.getPostId() to it.getEmojiType() }
            .mapValues { true }

        // 5. Post ID별로 ReactionType -> Count 맵 생성
        val reactionCountMap = reactionCounts
            .groupBy { it.getPostId() }
            .mapValues { (_, projections) ->
                projections.associate { it.getReactionType() to it.getCount().toInt() }
            }

        // 6. Post ID별, ReactionType별로 최근 반응자 리스트 생성 (최대 3명)
        val reactionReactorMap = reactionReactors
            .groupBy { it.getPostId() to it.getReactionType() }
            .mapValues { (_, projections) ->
                projections.take(3).map { it.getReactorNickname() }
            }

        // 7. Post 엔티티를 FeedResponse로 변환
        val dailyPosts = posts.map { post ->
            val postId = post.id ?: 0L
            val postCountMap = reactionCountMap[postId] ?: emptyMap()

            // 모든 ReactionType에 대해 ReactionInfo 생성
            val allReactions = ReactionType.entries.associateWith { type ->
                val count = postCountMap[type] ?: 0
                val reactors = reactionReactorMap[postId to type] ?: emptyList()
                val isMeReacted = myReactionMap[postId to type] ?: false
                HomeResponse.ReactionInfo(count, reactors, isMeReacted)
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

    /**
     * 내 기록 조회 (마이페이지용)
     * - 로그인한 사용자의 Base Date 이후 게시글
     * - 실제 Reaction 개수 집계
     * - 최신순 정렬
     *
     * @param member 조회할 사용자
     * @return FeedResponse 리스트
     */
    fun getMyFeedList(member: Member): List<HomeResponse.FeedResponse> {
        // 1. 사용자의 Base Date 이후 게시글 조회 (페이징 없이 전체)
        val baseDate = kkumoProperties.baseDate
        val posts = postRepository.findAllByMemberAndPostedDateGreaterThanEqualOrderByCreatedAtDesc(
            member,
            baseDate,
            Pageable.unpaged()
        ).content

        // 2. 모든 게시글의 리액션 개수 집계
        val reactionCounts = reactionRepository.countByPostsGroupByType(posts)

        // 3. 모든 게시글의 최근 반응자 조회 (Window Function으로 DB 레벨에서 TOP 3 추출)
        val postIds = posts.mapNotNull { it.id }
        val reactionReactors = reactionRepository.findRecentReactorsByPosts(postIds)

        // 4. 현재 사용자가 남긴 활성화된 리액션 조회 (N+1 방지: Projection 사용)
        val myActiveReactions = reactionRepository.findMyActiveReactions(member.id, postIds)
        val myReactionMap = myActiveReactions
            .groupBy { it.getPostId() to it.getEmojiType() }
            .mapValues { true }

        // 5. Post ID별로 ReactionType -> Count 맵 생성
        val reactionCountMap = reactionCounts
            .groupBy { it.getPostId() }
            .mapValues { (_, projections) ->
                projections.associate { it.getReactionType() to it.getCount().toInt() }
            }

        // 6. Post ID별, ReactionType별로 최근 반응자 리스트 생성 (최대 3명)
        val reactionReactorMap = reactionReactors
            .groupBy { it.getPostId() to it.getReactionType() }
            .mapValues { (_, projections) ->
                projections.take(3).map { it.getReactorNickname() }
            }

        // 7. Post 엔티티를 FeedResponse로 변환
        return posts.map { post ->
            val postId = post.id ?: 0L
            val postCountMap = reactionCountMap[postId] ?: emptyMap()

            // 모든 ReactionType에 대해 ReactionInfo 생성
            val allReactions = ReactionType.entries.associateWith { type ->
                val count = postCountMap[type] ?: 0
                val reactors = reactionReactorMap[postId to type] ?: emptyList()
                val isMeReacted = myReactionMap[postId to type] ?: false
                HomeResponse.ReactionInfo(count, reactors, isMeReacted)
            }

            HomeResponse.FeedResponse.from(post, allReactions)
        }
    }

    /**
     * 내 기록 조회 (마이페이지 무한 스크롤용)
     * - 로그인한 사용자의 Base Date 이후 게시글
     * - Slice 기반 페이징 (COUNT 쿼리 없음)
     * - 실제 Reaction 개수 집계
     * - 최신순 정렬
     *
     * @param member 조회할 사용자
     * @param pageable 페이징 정보
     * @return Slice<FeedResponse>
     */
    fun getMyFeedList(member: Member, pageable: Pageable): Slice<HomeResponse.FeedResponse> {
        // 1. 사용자의 Base Date 이후 게시글 조회 (Slice 사용)
        val baseDate = kkumoProperties.baseDate
        val postSlice = postRepository.findSliceByMemberAndPostedDateGreaterThanEqualOrderByCreatedAtDesc(
            member,
            baseDate,
            pageable
        )

        val posts = postSlice.content

        // 2. 모든 게시글의 리액션 개수 집계
        val reactionCounts = reactionRepository.countByPostsGroupByType(posts)

        // 3. 모든 게시글의 최근 반응자 조회 (Window Function으로 DB 레벨에서 TOP 3 추출)
        val postIds = posts.mapNotNull { it.id }
        val reactionReactors = reactionRepository.findRecentReactorsByPosts(postIds)

        // 4. 현재 사용자가 남긴 활성화된 리액션 조회 (N+1 방지: Projection 사용)
        val myActiveReactions = reactionRepository.findMyActiveReactions(member.id, postIds)
        val myReactionMap = myActiveReactions
            .groupBy { it.getPostId() to it.getEmojiType() }
            .mapValues { true }

        // 5. Post ID별로 ReactionType -> Count 맵 생성
        val reactionCountMap = reactionCounts
            .groupBy { it.getPostId() }
            .mapValues { (_, projections) ->
                projections.associate { it.getReactionType() to it.getCount().toInt() }
            }

        // 6. Post ID별, ReactionType별로 최근 반응자 리스트 생성 (최대 3명)
        val reactionReactorMap = reactionReactors
            .groupBy { it.getPostId() to it.getReactionType() }
            .mapValues { (_, projections) ->
                projections.take(3).map { it.getReactorNickname() }
            }

        // 7. Post 엔티티를 FeedResponse로 변환하여 Slice로 반환
        return postSlice.map { post ->
            val postId = post.id ?: 0L
            val postCountMap = reactionCountMap[postId] ?: emptyMap()

            // 모든 ReactionType에 대해 ReactionInfo 생성
            val allReactions = ReactionType.entries.associateWith { type ->
                val count = postCountMap[type] ?: 0
                val reactors = reactionReactorMap[postId to type] ?: emptyList()
                val isMeReacted = myReactionMap[postId to type] ?: false
                HomeResponse.ReactionInfo(count, reactors, isMeReacted)
            }

            HomeResponse.FeedResponse.from(post, allReactions)
        }
    }

    /**
     * 게시글 상세 조회
     * - 단일 게시글의 상세 정보 조회
     * - 비공개 게시글인 경우 작성자 본인만 조회 가능
     * - 실제 Reaction 개수 집계
     *
     * @param postId 조회할 게시글 ID
     * @param user 현재 로그인한 사용자
     * @return FeedResponse
     */
    fun getPostDetail(postId: Long, user: Member): HomeResponse.FeedResponse {
        // 1. 게시글 조회 (Member JOIN FETCH로 N+1 방지)
        val post = postRepository.findById(postId)
            .orElseThrow { BusinessException(ErrorCode.POST_NOT_FOUND) }

        // 2. 비공개 게시글 권한 체크
        if (post.member.id != user.id) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        // 3. 해당 게시글의 리액션 개수 집계
        val reactionCounts = reactionRepository.countByPostsGroupByType(listOf(post))

        // 4. 해당 게시글의 최근 반응자 조회 (Window Function으로 DB 레벨에서 TOP 3 추출)
        val postIds = listOfNotNull(post.id)
        val reactionReactors = reactionRepository.findRecentReactorsByPosts(postIds)

        // 5. 현재 사용자가 남긴 활성화된 리액션 조회
        val myActiveReactions = reactionRepository.findMyActiveReactions(user.id, postIds)
        val myReactionMap = myActiveReactions
            .groupBy { it.getPostId() to it.getEmojiType() }
            .mapValues { true }

        // 6. ReactionType -> Count 맵 생성
        val postCountMap = reactionCounts
            .associate { it.getReactionType() to it.getCount().toInt() }

        // 7. ReactionType별로 최근 반응자 리스트 생성 (최대 3명)
        val reactionReactorMap = reactionReactors
            .groupBy { it.getReactionType() }
            .mapValues { (_, projections) ->
                projections.take(3).map { it.getReactorNickname() }
            }

        // 8. Post 엔티티를 FeedResponse로 변환
        val allReactions = ReactionType.entries.associateWith { type ->
            val count = postCountMap[type] ?: 0
            val reactors = reactionReactorMap[type] ?: emptyList()
            val isMeReacted = myReactionMap[postId to type] ?: false
            HomeResponse.ReactionInfo(count, reactors, isMeReacted)
        }

        return HomeResponse.FeedResponse.from(post, allReactions)
    }
}
