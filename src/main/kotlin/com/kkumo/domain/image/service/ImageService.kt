package com.kkumo.domain.image.service

import org.springframework.web.multipart.MultipartFile

/**
 * 이미지 업로드 결과를 담는 데이터 클래스
 * @param thumbnailUrl 목록 표시용 썸네일 URL (압축/리사이징됨)
 * @param originalImageUrl 상세 조회용 원본 이미지 URL (원본 화질)
 */
data class ImageUploadResult(
    val thumbnailUrl: String,
    val originalImageUrl: String
)

/**
 * 이미지 업로드 서비스 인터페이스
 * 실제 구현체는 Cloudflare R2, S3 등을 사용할 수 있음
 */
interface ImageService {
    /**
     * 이미지 파일을 업로드하고 썸네일/원본 URL을 반환
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지의 썸네일 URL과 원본 URL
     */
    fun upload(file: MultipartFile): ImageUploadResult
}