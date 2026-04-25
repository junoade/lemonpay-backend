# LemonPay Backend

멀티통화 간편결제 시스템 백엔드 서비스.
원장(Ledger) 기반 잔액 모델과 분산 결제 흐름을 설계/구현하는 핀테크 사이드 프로젝트 입니다.

## 1. 프로젝트 목적
LemonPay는 단순 CRUD 결제 서비스가 아닌,
- 원장(Ledger) 기반 잔액 모델
- 멀티통화 정밀 계산
- 동시성 제어
- 분산 트랜잭션
- 외부 API 장애 대응(Circuit Breaker)

등을 직접 설계하고 구현해보는 것을 목표로 합니다.

금융 레거시 시스템에서의 운영 경험을 바탕으로 서비스형 아키텍처 설계를 연습하기 위해 개발했습니다.

## 2. 기술 스택
- **Language**: Java 21
- **Framework**: Spring Boot 3.4.3
- **Architecture**: Domain-Driven Design (DDD) / 레이어드 아키텍처
- **ORM**: Spring Data JPA / Hibernate
- **DB**: H2 (로컬) / MySQL (개발/운영)
- **Resilience**: Resilience4j (Retry, Circuit Breaker)
- **Build**: Gradle (Kotlin DSL)
- **Observability**: Spring Actuator / Prometheus


## 3. 핵심 설계 포인트

| 특징                     | 설명                                              |
|------------------------|-------------------------------------------------|
| **원장(Ledger) 기반 잔액 관리** | 잔액을 직접 수정하지 않고, 원장(LedgerEntry)에 이벤트를 쌓아 잔액을 계산 |
| **동시성 제어**             | 동시 결제 충돌 방지                                     |
| **분산 트랜잭션**            | 충전 → 환전 → 결제의 분산 트랜잭션 고려                        |
| **Circuit Breaker**    | Resilience4j를 활용한 외부 환율 API 장애 대응               |
| **멀티통화**               | KRW / USD / JPY, BigDecimal 기반 정밀 금액 처리         |


## 4. 아키텍처 개요
```
API → Application → Domain → Infrastructure
```


## 5. 패키지 구조

```
com.lemonpay/
├── member/        # 회원 도메인
├── wallet/        # 지갑/원장 도메인
├── payment/       # 결제 도메인 (Saga)
├── exchange/      # 환전 도메인
├── common/        # 공통 VO / 예외
└── api/           # REST Controller
```

## 6. 환경 구성

| 환경 | Branch | Profile | Infra | DB | API Docs |
|------|--------|---------|-------|-----|----------|
| 로컬 (기능개발) | feature/* | local | 로컬 실행 | H2 | Swagger ON |
| 개발 (스테이징) | dev | dev | docker-compose | MySQL | Swagger ON |
| 운영 | main | prod | EC2 + RDS | MySQL | 비활성 |

### 실행 방법

#### Dev 환경 실행

두 가지 방식으로 실행 지원

- 로컬 개발: MySQL만 컨테이너로 실행하고, 앱은 `bootRun`으로 실행
- 통합 실행: MySQL과 앱을 모두 컨테이너로 실행


#### (1) 로컬 개발 방식

1. 환경변수 파일을 준비합니다.

```bash
cp .env.dev.example .env.dev
```

2. MySQL 컨테이너를 실행합니다.

Make 명령:

```bash
make up-db
```

원본 명령:

```bash
docker compose -f docker/docker-compose.dev.yml --env-file .env.dev up -d mysql
```

3. Spring Boot 애플리케이션을 `dev` 프로필로 실행합니다.

Make 명령:

```bash
make run
```

원본 명령:

```bash
set -a && source .env.dev && set +a && SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

4. 실행 상태를 확인합니다.

Make 명령:

```bash
make ps
```

원본 명령:

```bash
docker compose -f docker/docker-compose.dev.yml --env-file .env.dev ps
```

- MySQL 컨테이너가 `healthy` 상태인지 확인합니다.
- Swagger UI: `http://localhost:8080/swagger-ui.html`

#### (2) 전체 컨테이너 실행 방식

1. 환경변수 파일을 준비합니다.

```bash
cp .env.dev.example .env.dev
```

2. MySQL + 앱 컨테이너를 함께 실행합니다.

Make 명령:

```bash
make up
```

원본 명령:

```bash
docker compose -f docker/docker-compose.dev.yml --env-file .env.dev up -d
```

3. 실행 상태를 확인합니다.

Make 명령:

```bash
make ps
```

원본 명령:

```bash
docker compose -f docker/docker-compose.dev.yml --env-file .env.dev ps
```

- `mysql` 컨테이너가 `healthy` 상태인지 확인합니다.
- `app` 컨테이너가 `Up` 상태이고, 로그상 Spring Boot가 정상 기동되었는지 확인합니다.
- Swagger UI: `http://localhost:8080/swagger-ui.html`

#### Make 명령어

| 명령어 | 설명 |
|--------|------|
| `make up` | MySQL + 앱 컨테이너 전체 실행 |
| `make up-db` | MySQL 컨테이너만 실행 |
| `make down` | 컨테이너 중지 및 제거 |
| `make ps` | 컨테이너 상태 확인 |
| `make logs` | 전체 컨테이너 로그 확인 |
| `make logs-app` | 앱 컨테이너 로그 확인 |
| `make logs-db` | MySQL 컨테이너 로그 확인 |
| `make build` | 백엔드 Docker 이미지 빌드 |
| `make run` | `.env.dev`를 읽어 `dev` 프로필로 `bootRun` 실행 |
| `make test` | dev 프로파일로 테스트 실행 (MySQL 컨테이너 필요) |
| `make test-local` | local 프로파일로 테스트 실행 (H2, MySQL 불필요) |

#### 주의사항

- 로컬에서 `3306` 포트를 이미 사용 중이면 MySQL 컨테이너가 기동되지 않을 수 있음.
- `.env.dev` 파일이 없으면 `dev` 프로필의 datasource 환경변수를 읽지 못해 애플리케이션이 실행되지 않음.
- `make test`는 `dev` 프로파일로 MySQL 컨테이너에 연결합니다. 실행 전 `make up-db` 등으로 MySQL 컨테이너가 기동되었는지 확인하세요. 
- MySQL 없이 테스트만 빠르게 실행하려면 `make test-local`을 사용하세요. (H2 인메모리 DB)


## 7. API Docs

Swagger:
```
http://localhost:8080/swagger-ui.html
```
| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/api/wallet/balances` | 전체 통화별 잔액 조회 |
| `POST` | `/api/wallet/charge` | 충전 |
| `GET` | `/api/wallet/transactions` | 거래 내역 조회 |
| `POST` | `/api/payment` | 결제 |
| `GET` | `/api/exchange/rate` | 환율 조회 |
| `POST` | `/api/exchange` | 환전 |




## 관련 프로젝트
- **Frontend**: [lemonpay-frontend](../lemonpay-frontend) — Vue 3 + Pinia + Tailwind CSS v4 
  - 프론트엔드의 경우 claude-code 활용 합니다.
