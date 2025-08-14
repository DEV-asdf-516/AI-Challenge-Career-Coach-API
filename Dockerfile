# 빌드 스테이지
FROM gradle:8.9-jdk17-alpine AS builder

WORKDIR /app

# Gradle 캐시 최적화 (빌드 속도 향상)
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle build -x test --no-daemon || return 0

# 소스코드 복사 및 빌드
COPY . .
RUN gradle build -x test --no-daemon

# 런타임 스테이지
FROM eclipse-temurin:17-jre-alpine

ARG PORT=9070
ENV PORT=${PORT}

RUN apk add --no-cache curl

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring

EXPOSE ${PORT}

ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx512m", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-jar", "app.jar"]
