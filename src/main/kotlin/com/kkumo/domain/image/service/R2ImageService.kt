package com.kkumo.domain.image.service

import com.kkumo.global.error.BusinessException
import com.kkumo.global.error.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.ByteArrayOutputStream
import java.util.UUID
import net.coobird.thumbnailator.Thumbnails
import java.io.ByteArrayInputStream

@Service
class R2ImageService(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucketName: String,
    @Value("\${cloud.aws.s3.public-domain}")
    private val publicDomain: String
): ImageService {

    override fun upload(file: MultipartFile): ImageUploadResult {
        // 1. 검증: 파일 비어있음 체크
        if (file.isEmpty) {
            throw BusinessException(ErrorCode.INVALID_INPUT, "업로드할 파일이 존재하지 않습니다.")
        }

        // 2. 검증: 이미지 MIME Type 체크
        val contentType = file.contentType
        if (contentType == null || !contentType.startsWith("image")) {
            throw BusinessException(ErrorCode.INVALID_INPUT, "이미지 파일만 업로드 가능합니다. (제출된 형식: $contentType)")
        }

        // 3. 파일명 생성 (UUID 기반, WebP 확장자)
        val fileName = "${UUID.randomUUID()}.webp"

        // 4. 이미지 최적화 (1080px, WebP, 품질 80%)
        val optimizedImageBytes = optimizeImage(file)

        // 5. R2에 업로드
        uploadToR2(fileName, optimizedImageBytes, "image/webp")
        val imageUrl = "$publicDomain$fileName"

        // 6. 썸네일과 원본 URL을 동일하게 반환
        return ImageUploadResult(
            thumbnailUrl = imageUrl,
            originalImageUrl = imageUrl
        )
    }

    /**
     * R2에 이미지 업로드
     */
    private fun uploadToR2(fileName: String, imageBytes: ByteArray, contentType: String) {
        val objectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(contentType)
            .build()

        runCatching {
            s3Client.putObject(
                objectRequest,
                RequestBody.fromInputStream(
                    ByteArrayInputStream(imageBytes),
                    imageBytes.size.toLong()
                )
            )
        }.onFailure { e ->
            throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 업로드 중 오류가 발생했습니다: ${e.message}")
        }
    }

    /**
     * 이미지 최적화 (1080px 리사이징, WebP 포맷, 품질 80%)
     */
    private fun optimizeImage(file: MultipartFile): ByteArray {
        val outputStream = ByteArrayOutputStream()

        try {
            Thumbnails.of(file.inputStream)
                .useExifOrientation(true)
                .size(1080, 1080)
                .outputFormat("webp")
                .outputQuality(0.8)
                .toOutputStream(outputStream)
        } catch (e: Exception) {
            // 최적화 실패 시 예외 발생
            throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 최적화 중 오류가 발생했습니다: ${e.message}")
        }

        return outputStream.toByteArray()
    }
}