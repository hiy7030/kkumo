# builder 단계
FROM eclipse-temurin:21-jdk AS builder

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    bash \
    curl \
    zip && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /spring-boot

COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

COPY settings.gradle.kts build.gradle.kts ./
RUN ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew clean build --no-daemon -x test

# runtime 단계
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    bash \
    curl && \
    rm -rf /var/lib/apt/lists/*

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /spring-boot

ARG JAR_FILE=/spring-boot/build/libs/*SNAPSHOT.jar
COPY --from=builder ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]