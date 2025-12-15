package com.kkumo.domain.home.controller

import com.kkumo.global.annotation.KKumoWebController
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@KKumoWebController
class HomeController {

    @GetMapping("/home")
    fun homePage(model: Model): String {
        // Mock: 오늘 기록 여부 (false로 설정하면 State 1, true로 설정하면 State 2)
        val hasPostedToday = true
        model.addAttribute("hasPostedToday", hasPostedToday)

        // Mock: 현재 Streak 연속 일수
        val currentStreak = 12
        model.addAttribute("currentStreak", currentStreak)

        // Mock: 내가 오늘 올린 사진 (hasPostedToday = true일 때만 사용)
        val myPost = FeedItem(
            id = 0,
            nickname = "나",
            emoji = "😊",
            imageUrl = "https://picsum.photos/seed/my-post/800/600",
            comment = "오늘도 열심히 살았어요!",
            reactions = mapOf("fire" to 5, "heart" to 12, "clap" to 3, "laugh" to 2)
        )
        model.addAttribute("myPost", myPost)

        // Mock: 다른 사람들의 피드 리스트 (10개 더미 데이터)
        val feedList = listOf(
            FeedItem(1, "강아지러버", "🐶", "https://picsum.photos/seed/1/800/600", "강아지와 산책했어요", mapOf("fire" to 3, "heart" to 8, "clap" to 2, "laugh" to 1)),
            FeedItem(2, "봄날의햇살", "🌸", "https://picsum.photos/seed/2/800/600", "봄이 왔네요!", mapOf("fire" to 5, "heart" to 15, "clap" to 7, "laugh" to 0)),
            FeedItem(3, "카페인중독", "☕", "https://picsum.photos/seed/3/800/600", "커피 한잔의 여유", mapOf("fire" to 2, "heart" to 4, "clap" to 1, "laugh" to 3)),
            FeedItem(4, "책벌레", "📚", "https://picsum.photos/seed/4/800/600", "독서하는 하루", mapOf("fire" to 7, "heart" to 10, "clap" to 5, "laugh" to 0)),
            FeedItem(5, "예술가", "🎨", "https://picsum.photos/seed/5/800/600", "그림 그리기 취미생활", mapOf("fire" to 12, "heart" to 20, "clap" to 8, "laugh" to 2)),
            FeedItem(6, "러닝맨", "🏃", "https://picsum.photos/seed/6/800/600", "아침 조깅 완료!", mapOf("fire" to 10, "heart" to 6, "clap" to 15, "laugh" to 1)),
            FeedItem(7, "라면왕", "🍜", "https://picsum.photos/seed/7/800/600", "맛있는 라면 한그릇", mapOf("fire" to 8, "heart" to 12, "clap" to 3, "laugh" to 5)),
            FeedItem(8, "달빛산책", "🌙", "https://picsum.photos/seed/8/800/600", "달빛 산책", mapOf("fire" to 4, "heart" to 18, "clap" to 6, "laugh" to 0)),
            FeedItem(9, "음악광", "🎵", "https://picsum.photos/seed/9/800/600", "음악 듣는 시간", mapOf("fire" to 6, "heart" to 9, "clap" to 4, "laugh" to 2)),
            FeedItem(10, "무지개헌터", "🌈", "https://picsum.photos/seed/10/800/600", "무지개를 봤어요", mapOf("fire" to 15, "heart" to 25, "clap" to 10, "laugh" to 8))
        )
        model.addAttribute("feedList", feedList)
        model.addAttribute("survivorCount", feedList.size)

        return "home"
    }

    // 더미 데이터용 DTO
    data class FeedItem(
        val id: Int,
        val nickname: String,
        val emoji: String,
        val imageUrl: String,
        val comment: String,
        val reactions: Map<String, Int> // fire, heart, clap, laugh
    )
}
