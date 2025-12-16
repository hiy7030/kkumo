package com.kkumo.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class R2Config(
    @Value("\${cloud.aws.s3.endpoint}")
    private val awsS3Endpoint: String,
    @Value("\${cloud.aws.credentials.accessKey}")
    private val accessKey: String,
    @Value("\${cloud.aws.credentials.secretKey}")
    private val secretKey: String,
    @Value("\${cloud.aws.region.static}")
    private val staticRegion: String
) {
    @Bean
    fun s3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)

        val endpoint = URI.create(awsS3Endpoint)

        return S3Client.builder()
            .endpointOverride(endpoint)
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(staticRegion)) // R2는 region을 'auto'로 설정
            .forcePathStyle(true) // 🚨 중요: R2는 Path Style Access만 지원함
            .build()
    }
}