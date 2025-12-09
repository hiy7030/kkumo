package com.kkumo.global.error

enum class ErrorCode(val message: String) {
    MEMBER_NOT_FOUND("회원을 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS("이미 존재하는 회원입니다."),
    EMAIL_ALREADY_EXISTS("이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS("이미 사용 중인 닉네임입니다."),
    EMOJI_ALREADY_EXISTS("이미 사용 중인 이모지입니다."),
    CROWN_EMOJI_NOT_ALLOWED("왕관 이모지는 시스템 전용입니다."),

    POST_NOT_FOUND("게시글을 찾을 수 없습니다."),
    POST_ALREADY_EXISTS_TODAY("오늘 이미 게시글을 작성했습니다."),
    POST_DELETE_NOT_ALLOWED("게시글 삭제는 허용되지 않습니다."),

    REACTION_NOT_FOUND("리액션을 찾을 수 없습니다."),

    INVALID_INPUT("잘못된 입력입니다."),
    UNAUTHORIZED("인증이 필요합니다."),
    FORBIDDEN("권한이 없습니다."),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다.")
}
