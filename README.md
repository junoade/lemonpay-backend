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