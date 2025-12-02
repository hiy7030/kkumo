package com.kkumo.domain.reaction

enum class ReactionType(
    val emoji: String,
    val description: String
) {
    LIKE("👍", "좋아요"),
    FIRE("🔥", "멋져요"),
    HEART("❤️", "사랑해요"),
    CLAP("👏", "축하해요"),
    SAD("😢", "슬퍼요");

    companion object {
        fun fromEmoji(emoji: String): ReactionType? {
            return entries.find { it.emoji == emoji }
        }
    }
}