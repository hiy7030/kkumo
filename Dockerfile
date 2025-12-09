# builder 단계
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

COPY settings.gradle.kts build.gradle.kts ./
RUN ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew clean build --no-daemon -x test

# runtime 단계
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 빌드 결과 JAR 복사 (SNAPSHOT 여부 상관없이)
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Cloudtype 환경변수 적용
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]