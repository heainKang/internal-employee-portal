# ── Stage 1: Build ──────────────────────────────────────────
FROM gradle:8.8-jdk17 AS build
WORKDIR /app

# 의존성 캐시를 위해 gradle 설정 파일 먼저 복사
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ ./gradle/

# 소스 복사 후 빌드 (테스트 제외)
COPY src ./src
RUN gradle bootJar -x test --no-daemon

# ── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

# plain jar 제외하고 실행 jar만 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
