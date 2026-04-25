# ---- Stage 1: Build ----
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# 의존성 캐시 최적화: gradle 설정만 먼저 복사
# 소스가 바뀌어도 이 레이어는 캐시 재사용
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 복사 후 빌드 (테스트 제외)
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ---- Stage 2: Run ----
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 보안: root 대신 전용 유저로 실행
RUN addgroup --system lemonpay && adduser --system --ingroup lemonpay lemonpay
USER lemonpay

# builder에서 JAR만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
