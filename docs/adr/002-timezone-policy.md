# ADR-002 Timezone Policy

## 1. 상태
- 결정완료

## 2. 컨텍스트
- 타임존은 DBMS(현 프로젝트 MySQL) / JVM / JDBC 커넥션 3개 레이어에서 각각 어긋날 수 있음.
- 토이 프로젝트 단계에서 사용자·운영자 모두 한국 기준이며, 시각 데이터 디버깅 편의 및 구현 속도 고려함.
- 글로벌 표준은 저장 UTC + 표시 시점 변환이지만, 현 단계에서는 과한 복잡도로 판단.

## 3. 결정
- 전 레이어를 Asia/Seoul로 고정한다.
    - MySQL: 컨테이너 TZ env → system_time_zone=KST, time_zone=SYSTEM
    - 운영 JVM: JAVA_TOOL_OPTIONS -Duser.timezone=Asia/Seoul (docker-compose.dev.yml)
    - 테스트 JVM: build.gradle.kts test 태스크 systemProperty("user.timezone", "Asia/Seoul")
      → 운영 JVM 존을 테스트가 미러링하여, 실행 머신/CI 러너 존(UTC 등)에 무관하게 결정론적으로 동작
    - JDBC: connectionTimeZone=Asia/Seoul 명시 (env DB_URL)
      - LocalDateTime 저장엔 무관하나, KST 약어 미인식 커넥션 오류 예방 목적
      - 세션 time_zone 강제는 아님(그건 forceConnectionTimeZoneToSession 필요)
- 엔티티 시각 타입은 LocalDateTime을 사용한다.

## 4. 검증 
- 회귀 테스트: `TimezoneConfigTest` (Testcontainers MySQL 8.0 기반, 컨테이너 TZ=Asia/Seoul)
    - 저장 시각(created_at)이 DB 서버 시각(NOW())과 어긋나지 않음을 검증
      (JVM 시계를 비교에 끼우지 않고, TIMESTAMPDIFF로 DB 내부에서 비교)
    - 진단용: @@system_time_zone == KST 확인 (컨테이너 TZ env 반영 여부)
- 테스트 유효성 확인: JVM 존을 강제로 UTC로 두면 실패함을 확인
    - `./gradlew cleanTest test --tests "*TimezoneConfigTest" -Duser.timezone=UTC` → created_at(UTC) vs NOW()(KST) 9h 차이로 실패
    - 즉 이 테스트는 존 정렬을 실제로 검증하며, 공허한 통과(vacuous green)가 아님을 입증함

## 5. 한계
- 글로벌 확장 시 Instant(UTC) 저장으로 전환, 기존 데이터는 -9h 마이그레이션 필요. 이 비용을 인지하고 현 단계 단순함을 택함.
- created_at은 LocalDateTime + @CreatedDate(JVM 시계 기반)이므로, 저장되는 벽시계 값은 JVM 기본 존에 의존한다.
    - connectionTimeZone은 instant 계열 변환에만 관여하며 LocalDateTime 저장 리터럴은 변환하지 않음 → 저장 시각을 좌우하는 실질 레버는 JVM 존이다.
    - 따라서 운영에서 JAVA_TOOL_OPTIONS가 유실되면 시각이 조용히 UTC로 저장될 수 있으며, JVM 존을 KST로 고정한 현재 테스트는 이 상황을 잡지 못한다.
    - 근본 방어는 audit 필드를 Instant(UTC)로 전환하는 것(별도 이슈로 검토).