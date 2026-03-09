# LemonPay 요구사항 명세서

> 멀티통화 간편결제 시스템 - 요구사항 문서
> 버전: 1.0 | 최종 수정일: 2026-03-04 | 상태: 초안

---

## 1. 유저 스토리 (User Stories)

### 1.1 지갑 (Wallet)

| ID | 유저 스토리 | 우선순위 |
|----|-----------|---------|
| US-W01 | **회원**으로서, **여러 통화(KRW, USD, JPY)의 지갑 잔액을 한눈에 확인**하고 싶다. **사용 가능한 자금을 빠르게 파악하기 위해서**이다. | Must |
| US-W02 | **회원**으로서, **지갑에 KRW를 충전**하고 싶다. **결제와 환전에 사용하기 위해서**이다. | Must |
| US-W03 | **회원**으로서, **지갑의 거래 내역(충전, 결제, 환전)을 조회**하고 싶다. **모든 자금 이동을 추적하기 위해서**이다. | Must |
| US-W04 | **회원**으로서, **거래 후 잔액 변동이 실시간으로 반영되는 것을 확인**하고 싶다. **항상 정확한 잔액을 파악하기 위해서**이다. | Should |

### 1.2 결제 (Payment)

| ID | 유저 스토리 | 우선순위 |
|----|-----------|---------|
| US-P01 | **회원**으로서, **KRW 잔액을 사용하여 외화(USD, JPY)로 가맹점에 결제**하고 싶다. **다통화 결제를 편리하게 처리하기 위해서**이다. | Must |
| US-P02 | **회원**으로서, **결제 확인 전에 예상 KRW 차감 금액을 확인**하고 싶다. **충분한 정보를 바탕으로 결제 여부를 결정하기 위해서**이다. | Must |
| US-P03 | **회원**으로서, **잔액이 부족할 때 명확한 오류 메시지를 받고** 싶다. **결제 실패 사유를 이해하기 위해서**이다. | Must |
| US-P04 | **회원**으로서, **결제 상태 전환(대기 -> 처리 중 -> 완료/실패)을 확인**하고 싶다. **현재 거래 상태를 파악하기 위해서**이다. | Should |
| US-P05 | **회원**으로서, **결제 시 만료된(stale) 환율이 사용되었을 경우 알림을 받고** 싶다. **환율 차이 가능성을 인지하기 위해서**이다. | Could |

### 1.3 환전 (Exchange)

| ID | 유저 스토리 | 우선순위 |
|----|-----------|---------|
| US-E01 | **회원**으로서, **KRW를 외화(USD, JPY)로 환전**하고 싶다. **여러 통화로 잔액을 보유하기 위해서**이다. | Must |
| US-E02 | **회원**으로서, **환전 확인 전에 현재 환율과 예상 변환 금액을 확인**하고 싶다. **환전 진행 여부를 판단하기 위해서**이다. | Must |
| US-E03 | **회원**으로서, **지원하는 모든 통화 간에 환전**하고 싶다. **멀티통화 지갑을 유연하게 관리하기 위해서**이다. | Could |

### 1.4 회원 (Member)

| ID | 유저 스토리 | 우선순위 |
|----|-----------|---------|
| US-M01 | **신규 사용자**로서, **회원가입 시 지갑이 자동으로 생성**되기를 원한다. **바로 서비스를 이용하기 위해서**이다. | Must |
| US-M02 | **회원**으로서, **로그인하여 지갑과 거래 내역에 접근**하고 싶다. **금융 데이터를 안전하게 보호하기 위해서**이다. | Must |

---

## 2. 기능적 요구사항 (Functional Requirements)

### 2.1 지갑 도메인 (Wallet Domain)

| ID | 요구사항 | 수용 기준 (Acceptance Criteria) |
|----|---------|-------------------------------|
| FR-001 | 시스템은 회원 가입 시 지갑을 자동으로 생성해야 한다. | - 회원당 지갑 1개 (1:1 관계) <br> - 지갑은 ACTIVE 상태로 생성 <br> - 초기 잔액: KRW 0, USD 0, JPY 0 |
| FR-002 | 시스템은 단일 지갑 내에서 멀티통화 잔액을 지원해야 한다. | - 지원 통화: KRW, USD, JPY <br> - 각 통화 잔액은 `wallet_balance` 테이블에서 독립적으로 관리 <br> - 잔액은 `BigDecimal` scale 4 (DECIMAL(18,4))로 저장 |
| FR-003 | 시스템은 단순 UPDATE가 아닌 **Ledger(원장, insert-only) 패턴**으로 잔액을 관리해야 한다. | - 모든 잔액 변동 시 `ledger_entry` 레코드 생성 <br> - 원장 항목은 불변 (INSERT만 허용, UPDATE/DELETE 금지) <br> - 각 항목에 금액, 방향(CREDIT/DEBIT), 거래 후 잔액(balance_after) 기록 <br> - `wallet_balance`의 스냅샷 잔액은 원장 INSERT와 원자적(atomic)으로 갱신 |
| FR-004 | 시스템은 KRW 지갑 충전(top-up)을 지원해야 한다. | - entry_type=CHARGE, direction=CREDIT인 원장 항목 생성 <br> - `wallet_balance` 스냅샷을 원자적으로 갱신 <br> - 최소 충전 금액: 1,000 KRW <br> - 건당 최대 충전 금액: 10,000,000 KRW |
| FR-005 | 시스템은 페이지네이션이 포함된 거래 내역 조회 API를 제공해야 한다. | - 원장 항목을 최신순(역순)으로 반환 <br> - 커서 기반 또는 오프셋 기반 페이지네이션 지원 <br> - 통화 및 entry_type 필터링 가능 |
| FR-006 | 시스템은 스냅샷 잔액과 원장 합산 간의 **대사(Reconciliation)**를 구현해야 한다. | - `wallet_balance.balance`와 원장 항목 SUM의 정기 비교 스케줄링 <br> - 불일치 감지 시 로그 기록 <br> - 자동 보정 없음 (수동 검토 필요) |

### 2.2 결제 도메인 (Payment Domain)

| ID | 요구사항 | 수용 기준 (Acceptance Criteria) |
|----|---------|-------------------------------|
| FR-101 | 시스템은 **Saga 패턴**을 사용하여 다통화 결제를 처리해야 한다. | - Saga 단계: (1) 지갑에서 KRW 차감 -> (2) 환전 실행 -> (3) 결제 처리 <br> - 각 단계에 대응하는 보상(compensation) 액션 존재 <br> - 실패 시 역순으로 보상 실행 |
| FR-102 | 시스템은 결제 시작 전에 충분한 잔액을 검증해야 한다. | - 사전 검증: `wallet_balance.balance >= required_amount` <br> - 실제 차감은 Race Condition 방지를 위해 Optimistic Locking 사용 <br> - 잔액 부족 시 명확한 오류 코드(INSUFFICIENT_BALANCE) 반환 |
| FR-103 | 시스템은 정의된 상태 머신(State Machine)을 통해 결제 상태를 추적해야 한다. | - 상태: PENDING -> APPROVED -> COMPLETED / FAILED / CANCELLED <br> - FAILED 결제는 보상(잔액 복원) 트리거 <br> - CANCELLED는 PENDING 상태에서만 가능 |
| FR-104 | 시스템은 다통화 결제의 정산 상세 정보를 기록해야 한다. | - 저장 항목: settlement_amount, settlement_currency, exchange_rate <br> - 해당 시 exchange_transaction과 연결 |
| FR-105 | 시스템은 중복 결제 요청을 방지해야 한다. | - 결제 요청별 멱등성 키(Idempotency Key) <br> - 24시간 이내 동일 키에 대해 기존 결제 결과 반환 |

### 2.3 환전 도메인 (Exchange Domain)

| ID | 요구사항 | 수용 기준 (Acceptance Criteria) |
|----|---------|-------------------------------|
| FR-201 | 시스템은 외부 API(exchangerate-api.com)에서 환율을 조회해야 한다. | - 설정 가능한 TTL로 환율 캐싱 (기본값: 10분) <br> - 외부 API 불가 시 만료된 캐시로 폴백(fallback) <br> - rate_source 기록: REALTIME, CACHED, 또는 FALLBACK |
| FR-202 | 시스템은 환율 API에 **Circuit Breaker**를 구현해야 한다. | - Resilience4j CircuitBreaker 사용 <br> - 연속 5회 실패 후 CLOSED -> OPEN 전환 <br> - 30초 후 OPEN -> HALF_OPEN 전환 <br> - 성공 3회 후 HALF_OPEN -> CLOSED 전환 <br> - 폴백: 만료된 캐시 환율 또는 DB 마지막 저장 환율 반환 |
| FR-203 | 시스템은 환전을 원자적(atomic) 연산으로 처리해야 한다. | - 단일 트랜잭션에서 두 개의 원장 항목 생성: <br>  (1) 출금 통화에 EXCHANGE_OUT (DEBIT) <br>  (2) 입금 통화에 EXCHANGE_IN (CREDIT) <br> - 두 항목은 동일한 transaction_id 공유 |
| FR-204 | 시스템은 BigDecimal 연산으로 환전 금액을 계산해야 한다. | - 금융 계산에 부동소수점(double/float) 사용 금지 <br> - 반올림 모드: HALF_UP <br> - KRW: scale 0, USD: scale 2, JPY: scale 0 |

### 2.4 동시성 제어 (Concurrency Control)

| ID | 요구사항 | 수용 기준 (Acceptance Criteria) |
|----|---------|-------------------------------|
| FR-301 | 시스템은 동시 잔액 수정을 처리하기 위해 **Optimistic Locking(낙관적 잠금)**을 사용해야 한다. | - `wallet_balance`에 `version` 컬럼 (JPA `@Version`) <br> - 동시 수정 시 `OptimisticLockException` 발생 <br> - 클라이언트에 HTTP 409 Conflict와 재시도 안내 반환 |
| FR-302 | 시스템은 Optimistic Lock 실패 시 자동 재시도를 지원해야 한다. | - 서버 측 재시도: 지수 백오프(exponential backoff)로 최대 3회 <br> - 모든 재시도 소진 시 클라이언트에 오류 반환 <br> - 매 재시도마다 최신 잔액 재조회 및 재검증 |

### 2.5 회원 도메인 (Member Domain)

| ID | 요구사항 | 수용 기준 (Acceptance Criteria) |
|----|---------|-------------------------------|
| FR-401 | 시스템은 이메일 기반 회원 가입을 지원해야 한다. | - 이메일 유니크 제약 조건 <br> - 회원 상태: ACTIVE, SUSPENDED, CLOSED <br> - 가입 시 지갑 자동 생성 (FR-001) |
| FR-402 | 시스템은 기본 인증(authentication)을 지원해야 한다. | - 세션 기반 또는 토큰 기반 인증 (포트폴리오 범위) <br> - 보호된 엔드포인트는 유효한 인증 필요 |

---

## 3. 비기능적 요구사항 (Non-Functional Requirements)

### 3.1 성능 (Performance)

| ID | 요구사항 | 목표 | 근거 |
|----|---------|------|------|
| NFR-001 | 결제 API 응답 시간 | p50 < 100ms, p99 < 500ms | 다통화 결제는 환율 조회 + Saga 실행을 포함한다. p99 500ms는 캐시 미스 + 외부 API 호출을 고려한 수치이다. |
| NFR-002 | 지갑 잔액 조회 응답 시간 | p50 < 30ms, p99 < 100ms | 스냅샷 기반 조회(원장 집계 아님)로 빠른 읽기를 가능하게 한다. |
| NFR-003 | 환율 API 폴백 지연시간 | < 50ms | 캐시/DB 폴백은 외부 API 호출보다 충분히 빨라야 Graceful Degradation이 보장된다. |
| NFR-004 | 동시 결제 처리량 | 지갑당 >= 50 TPS | Optimistic Locking이 일반적인 동시 접근을 병목 없이 처리해야 한다. |

### 3.2 신뢰성 및 가용성 (Reliability & Availability)

| ID | 요구사항 | 목표 | 근거 |
|----|---------|------|------|
| NFR-101 | Saga 보상 트랜잭션 성공률 | 잔액 복원 100% | 실패한 결제는 차감된 잔액을 반드시 복원해야 한다. 금융 시스템에서 데이터 정합성은 타협 불가이다. |
| NFR-102 | Circuit Breaker 복구 | API 복구 후 60초 이내 자동 회복 | HALF_OPEN 상태가 30초마다 탐색하므로, 2회 탐색 주기 내 완전 복구. |
| NFR-103 | 데이터 정합성 (스냅샷 vs 원장) | 불일치 허용치 0 | 대사 배치가 모든 불일치를 감지한다. 운영 환경에서는 알림을 트리거해야 한다. |

### 3.3 보안 (Security)

| ID | 요구사항 | 목표 | 근거 |
|----|---------|------|------|
| NFR-201 | 원장 불변성 (Ledger Immutability) | ledger_entry 테이블에 UPDATE 또는 DELETE 금지 | 금융 감사 추적(audit trail)은 추가 전용(append-only) 기록을 요구한다. 수정은 보상 항목(compensating entry)으로 처리한다. |
| NFR-202 | 잔액 변경 인가 | 인증된 회원만 본인 지갑 변경 가능 | 미인가 자금 이전을 방지한다. |
| NFR-203 | 금액 입력 검증 | 모든 금액 입력에 대해 서버 측 검증 | 음수 금액, 오버플로, 정밀도 공격을 방지한다. 금액 > 0, 설정된 한도 이내. |
| NFR-204 | API 호출 제한 (Rate Limiting) | 회원당 최대 100 요청/분 | 결제 엔드포인트에 대한 남용 및 무차별 대입 공격을 방지한다. |

### 3.4 확장성 (Scalability)

| ID | 요구사항 | 목표 | 근거 |
|----|---------|------|------|
| NFR-301 | 통화 확장성 | 설정 변경만으로 신규 통화 추가 | 신규 통화 지원에 코드 변경 불필요. 통화 메타데이터(코드, scale, 기호)는 데이터 기반으로 관리. |
| NFR-302 | 원장 데이터 증가 대응 | 1,000만 건 이상에서도 조회 성능 저하 없음 | (wallet_id, currency, created_at) 인덱싱. 스냅샷 잔액으로 전체 테이블 집계를 회피. |
| NFR-303 | 모듈형 아키텍처 | 도메인 계층의 인프라 의존성 0 | DDD 계층 아키텍처: domain -> application -> infrastructure. 향후 멀티 모듈 또는 MSA 전환 가능. |

### 3.5 관측성 (Observability)

| ID | 요구사항 | 목표 | 근거 |
|----|---------|------|------|
| NFR-401 | 만료 환율 사용 추적 | FALLBACK 또는 CACHED 환율 사용 결제 건 전수 로깅 | 사후 분석 및 환율 차액 정산 가능하게 한다. |
| NFR-402 | Circuit Breaker 상태 변경 로깅 | 모든 상태 전환(CLOSED/OPEN/HALF_OPEN) 로깅 | 운영 모니터링 및 장애 대응에 필수적이다. |
| NFR-403 | Optimistic Lock 충돌 로깅 | 모든 충돌 발생 시 wallet_id와 재시도 횟수 로깅 | 용량 계획 및 동시성 패턴 분석을 가능하게 한다. |

### 3.6 유지보수성 (Maintainability)

| ID | 요구사항 | 목표 | 근거 |
|----|---------|------|------|
| NFR-501 | 테스트 커버리지 | 도메인 및 애플리케이션 계층 라인 커버리지 >= 80% | 핵심 비즈니스 로직(잔액 계산, Saga, 환전)은 철저히 테스트되어야 한다. |
| NFR-502 | API 문서화 | 코드에서 자동 생성되는 OpenAPI 3.0 명세 | SpringDoc 연동으로 문서와 구현의 동기화를 보장한다. |

---

## 4. 도메인 모델 요약

### 4.1 핵심 도메인

```
+------------------+     +------------------+     +------------------+
|     Member       |     |     Wallet       |     |  WalletBalance   |
|  (Aggregate Root)|---->|  (Aggregate Root)|---->|   (Entity)       |
|                  |     |                  |     |                  |
|  - email         |     |  - status        |     |  - currency      |
|  - status        |     |  - member (1:1)  |     |  - balance       |
|  - createdAt     |     |  - createdAt     |     |  - version (OL)  |
+------------------+     +--------+---------+     +------------------+
                                  |
                    +-------------+-------------+
                    |                           |
           +--------+--------+       +---------+---------+
           |   LedgerEntry   |       | PaymentTransaction|
           |   (Entity)      |       |  (Aggregate Root) |
           |                 |       |                   |
           |  - entryType    |       |  - amount/currency|
           |  - amount       |       |  - settlementAmt  |
           |  - direction    |       |  - exchangeRate   |
           |  - balanceAfter |       |  - status (FSM)   |
           |  - transactionId|       |  - merchantId     |
           +-----------------+       +-------------------+
                                              |
                                    +---------+---------+
                                    |ExchangeTransaction|
                                    |  (Aggregate Root) |
                                    |                   |
                                    |  - sourceCurrency |
                                    |  - targetCurrency |
                                    |  - exchangeRate   |
                                    |  - rateSource     |
                                    +-------------------+
```

### 4.2 Aggregate 경계

| Aggregate | 루트 엔티티 | 소유 엔티티 | 불변 조건 (Invariants) |
|-----------|-----------|-----------|----------------------|
| **Member** | Member | - | 이메일 유일성, 상태 전이 규칙 |
| **Wallet** | Wallet | WalletBalance, LedgerEntry | 잔액 >= 0, 원장-스냅샷 정합성, 버전 무결성 |
| **Payment** | PaymentTransaction | - | 상태 머신(State Machine), 멱등성 키 유일성 |
| **Exchange** | ExchangeTransaction | - | source_amount * exchange_rate = target_amount (반올림 허용 범위 내) |

### 4.3 핵심 도메인 규칙

1. **잔액 불변 조건**: `wallet_balance.balance`는 동일 지갑 및 통화의 `SUM(ledger_entries)`와 항상 일치해야 한다.
2. **원장 불변성**: 원장 항목은 수정하거나 삭제할 수 없다. 정정은 새로운 보상 항목(compensating entry)으로 기록한다.
3. **Saga 원자성**: 결제 Saga는 모든 단계를 완료하거나, 완료된 모든 단계를 보상해야 한다. 부분 상태(partial state)는 허용되지 않는다.
4. **통화 정밀도**: KRW와 JPY는 scale 0(소수점 없음), USD는 scale 2를 사용한다. 모든 계산은 `BigDecimal`과 `RoundingMode.HALF_UP`을 사용한다.
5. **Optimistic Lock 보장**: 동일 `wallet_balance` 행에 대한 두 동시 수정이 모두 성공할 수 없다. 느린 쪽의 트랜잭션은 재시도하거나 실패해야 한다.

---

## 5. API 엔드포인트 개요

> 상세 API 명세는 `docs/api-spec.md`에서 관리한다. 아래는 요구사항 추적을 위한 요약이다.

| 메서드 | 엔드포인트 | 설명 | 관련 FR |
|--------|----------|------|---------|
| POST | `/api/v1/members` | 회원 가입 | FR-401 |
| POST | `/api/v1/members/login` | 인증 (로그인) | FR-402 |
| GET | `/api/v1/wallets/{memberId}` | 지갑 및 전체 통화 잔액 조회 | FR-001, FR-002 |
| POST | `/api/v1/wallets/{walletId}/charge` | KRW 잔액 충전 | FR-004 |
| GET | `/api/v1/wallets/{walletId}/transactions` | 거래 내역 조회 (페이지네이션) | FR-005 |
| POST | `/api/v1/payments` | 다통화 결제 시작 (Saga) | FR-101, FR-102 |
| GET | `/api/v1/payments/{paymentId}` | 결제 상태 조회 | FR-103 |
| GET | `/api/v1/exchange-rates` | 현재 환율 조회 | FR-201 |
| POST | `/api/v1/exchanges` | 환전 실행 | FR-203 |

---

## 6. 추적 매트릭스 (Traceability Matrix)

| 유저 스토리 | 기능적 요구사항 | 비기능적 요구사항 | 스프린트 |
|-----------|--------------|----------------|---------|
| US-W01 | FR-001, FR-002 | NFR-002 | Sprint 1 |
| US-W02 | FR-004 | NFR-203 | Sprint 2 |
| US-W03 | FR-005 | NFR-302 | Sprint 1 |
| US-W04 | FR-003 | NFR-103 | Sprint 2 |
| US-P01 | FR-101, FR-102, FR-104 | NFR-001, NFR-101 | Sprint 2 |
| US-P02 | FR-201 | NFR-003 | Sprint 2 |
| US-P03 | FR-102 | NFR-203 | Sprint 2 |
| US-P04 | FR-103 | - | Sprint 2 |
| US-P05 | FR-202 | NFR-401 | Sprint 3 |
| US-E01 | FR-203, FR-204 | NFR-001 | Sprint 2 |
| US-E02 | FR-201 | NFR-003 | Sprint 2 |
| US-E03 | FR-203 | NFR-301 | Sprint 3 |
| US-M01 | FR-001, FR-401 | - | Sprint 1 |
| US-M02 | FR-402 | NFR-202, NFR-204 | Sprint 1 |

---

## 부록 A: 용어 사전

| 용어 | 정의 |
|------|------|
| **원장 항목 (Ledger Entry)** | 단일 잔액 변동의 불변 기록. 모든 입금(credit) 또는 출금(debit)은 정확히 하나의 항목을 생성한다. |
| **스냅샷 잔액 (Snapshot Balance)** | 빠른 조회를 위해 `wallet_balance`에 미리 계산하여 저장한 현재 잔액. 원자적 트랜잭션을 통해 원장과 동기화를 유지한다. |
| **Saga** | 각 단계에 실패 시 효과를 되돌리는 보상 액션이 있는 로컬 트랜잭션의 연속 실행 패턴. |
| **Optimistic Locking (낙관적 잠금)** | 사전에 잠금을 획득하지 않고, 커밋 시점에 버전 카운터를 사용하여 충돌을 감지하는 동시성 제어 전략. |
| **Circuit Breaker (회로 차단기)** | 비정상 외부 의존성에 대한 호출을 단락(short-circuit)시켜 장애 전파(cascading failure)를 방지하는 안정성 패턴. |
| **보상 (Compensation)** | 이전에 완료된 Saga 단계의 효과를 되돌리는 역연산. 예: 차감된 잔액 복원. |
| **대사 (Reconciliation)** | 스냅샷 잔액과 원장 항목 합산 간의 정합성을 검증하는 정기 프로세스. |
| **멱등성 키 (Idempotency Key)** | 클라이언트가 재시도하더라도 동일 연산이 중복 실행되지 않도록 보장하는 요청별 고유 식별자. |
| **환율 출처 (Rate Source)** | 환율 획득 방식 표시: REALTIME(실시간 API), CACHED(TTL 이내 캐시), FALLBACK(만료 캐시/DB). |

---

## 부록 B: 범위 외 항목

- **복식부기 (Double-entry bookkeeping)**: 단방향 원장을 차변/대변 계정 쌍으로 확장
- **이벤트 소싱 (Event Sourcing)**: 원장 항목을 도메인 이벤트로 처리하여 전체 상태 재구성
- **CQRS**: 조회 최적화를 위한 읽기/쓰기 모델 분리
- **분산 락 (Redisson)**: 높은 경합 상황에서 Optimistic Locking의 대안
- **정산 배치 (Settlement Batch)**: 만료 환율 결제 건과 실제 환율 간 차액 정기 정산
- **PCI DSS 준수**: 카드 데이터 처리 표준 (잔액 기반 결제에는 해당 없음)
- **멀티 테넌트 아키텍처**: 다수의 결제 서비스 사업자 지원

---

*LemonPay PM Agent | 요구사항 명세서 v1.0 | 2026-03-04*
