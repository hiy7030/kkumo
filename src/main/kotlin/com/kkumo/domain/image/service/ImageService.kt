package com.kkumo.domain.image.service

import org.springframework.web.multipart.MultipartFile

/**
 * 이미지 업로드 서비스 인터페이스
 * 실제 구현체는 Cloudflare R2, S3 등을 사용할 수 있음
 */
interface ImageService {
    /**
     * 이미지 파일을 업로드하고 URL을 반환
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지의 URL
     */
    fun upload(file: MultipartFile): String
}