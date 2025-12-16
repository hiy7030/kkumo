package com.kkumo.domain.image.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

@Service
class R2ImageService(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucketName: String,
    @Value("\${cloud.aws.s3.publicUrl}")
    private val publicUrl: String
): ImageService {

    override fun upload(file: MultipartFile): String {
        // 1. 파일명 중복 방지를 위한 UUID 생성
        // 예: a1b2c3d4-test.jpg
        val originalFilename = file.originalFilename ?: "unknown.jpg"
        val extension = originalFilename.substringAfterLast(".", "jpg")
        val savedFileName = "${UUID.randomUUID()}.$extension"

        // 2. 메타데이터 설정 (브라우저에서 바로 보기 위해 Content-Type 필수)
        val objectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(savedFileName)
            .contentType(file.contentType ?: "application/octet-stream")
            // .acl(ObjectCannedACL.PUBLIC_READ) // R2는 버킷 설정에서 Public을 켜야 함 (코드로 제어 X)
            .build()

        // 3. R2로 업로드
        runCatching {
            s3Client.putObject(
                objectRequest,
                RequestBody.fromInputStream(file.inputStream, file.size)
            )
        }.onFailure { e ->
            throw RuntimeException("이미지 업로드 실패: ${e.message}", e)
        }

        // 4. 조회 가능한 Public URL 반환
        // 예: https://img.kkumo.com/a1b2c3d4.jpg
        return "$publicUrl/kkumo/$savedFileName"
    }
}