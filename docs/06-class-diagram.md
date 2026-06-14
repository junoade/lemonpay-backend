# LemonPay 도메인 클래스 다이어그램

> 버전: 1.0 | Sprint 1 기준 | 최종 수정: 2026-03-28
> 코드 변경 시 Sprint 종료 시점에 업데이트

---

## 1. Common (공유 VO)

```mermaid
classDiagram
    class Currency {
        <<enumeration>>
        KRW
        USD
        JPY
        -scale int
        -name String
        -code String
        -symbol String
        +getScale() int
        +getSymbol() String
    }

    class Money {
        <<record>>
        -amount BigDecimal
        -currency Currency
        +of(BigDecimal, Currency) Money$
        +won(long) Money$
        +usd(String) Money$
        +jpy(long) Money$
        +zero(Currency) Money$
        +add(Money) Money
        +subtract(Money) Money
        +isGreaterThan(Money) boolean
        +isZero() boolean
    }

    Money *--> Currency : contains
```

---

## 2. Wallet Context

```mermaid
classDiagram
    class Member {
        <<entity>>
        -UUID id
        -String email
        -String name
        -String phone
        -MemberStatus status
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }

    class MemberStatus {
        <<enumeration>>
        ACTIVE
        SUSPENDED
        CLOSED
    }

    class Wallet {
        <<entity>>
        -UUID id
        -UUID memberId
        -String name
        -boolean isPrimary
        -String productCode
        -WalletStatus status
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }

    class WalletStatus {
        <<enumeration>>
        ACTIVE
        FROZEN
        CLOSED
    }

    class WalletBalance {
        <<entity>>
        -Long id
        -UUID walletId
        -Currency currency
        -BigDecimal balance
        -Long version
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }

    Member "1" --> "1" Wallet : owns
    Member --> MemberStatus
    Wallet "1" --> "1..*" WalletBalance : contains
    Wallet --> WalletStatus
    WalletBalance --> Currency
```

---

## 3. Ledger Context

```mermaid
classDiagram
    class LedgerEntry {
        <<entity>>
        -Long id
        -UUID walletId
        -Currency currency
        -EntryType entryType
        -BigDecimal amount
        -Direction direction
        -BigDecimal balanceAfter
        -String description
        -String transactionId
        -Long relatedEntryId
        -String externalRefId
        -LocalDateTime createdAt
    }

    class Direction {
        <<enumeration>>
        CREDIT
        DEBIT
    }

    class EntryType {
        <<enumeration>>
        CHARGE
        WITHDRAW
        PAYMENT
        PAYMENT_CANCEL
        REFUND
        EXCHANGE_OUT
        EXCHANGE_IN
        -direction Direction
        +getDirection() Direction
    }

    LedgerEntry --> EntryType
    LedgerEntry --> Direction
    LedgerEntry --> Currency
    EntryType o--> Direction : has
```

---

## 4. Payment Context

```mermaid
classDiagram
    class PaymentTransaction {
        <<entity>>
        -Long id
        -String txNo
        -UUID walletId
        -BigDecimal amount
        -Currency currency
        -BigDecimal settlementAmount
        -Currency settlementCurrency
        -BigDecimal exchangeRate
        -PaymentStatus status
        -String idempotencyKey
        -String merchantId
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -LocalDateTime completedAt
    }

    class PaymentStatus {
        <<enumeration>>
        PENDING
        COMPLETED
        FAILED
        CANCELLED
    }

    PaymentTransaction --> PaymentStatus
    PaymentTransaction --> Currency
```

---

## 5. Exchange Context

```mermaid
classDiagram
    class ExchangeTransaction {
        <<entity>>
        -Long id
        -String txNo
        -UUID walletId
        -Currency sourceCurrency
        -BigDecimal sourceAmount
        -Currency targetCurrency
        -BigDecimal targetAmount
        -BigDecimal exchangeRate
        -RateSource rateSource
        -ExchangeStatus status
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }

    class RateSource {
        <<enumeration>>
        REALTIME
        CACHED
        FALLBACK
    }

    class ExchangeStatus {
        <<enumeration>>
        PENDING
        COMPLETED
        FAILED
    }

    ExchangeTransaction --> RateSource
    ExchangeTransaction --> ExchangeStatus
    ExchangeTransaction --> Currency
```

---

## 6. 전체 관계도

```mermaid
classDiagram
    Member "1" --> "1" Wallet
    Wallet "1" --> "1..*" WalletBalance
    Wallet "1" --> "0..*" LedgerEntry
    Wallet "1" --> "0..*" PaymentTransaction
    Wallet "1" --> "0..*" ExchangeTransaction
    WalletBalance --> Currency
    LedgerEntry --> EntryType
    LedgerEntry --> Currency
    PaymentTransaction --> PaymentStatus
    ExchangeTransaction --> RateSource
    Money *--> Currency
```

---

## 7. 추적 매트릭스

| 클래스 | 관련 FR | ERD 테이블 | 핵심 불변 조건 |
|--------|---------|-----------|--------------|
| Money | FR-204 | DECIMAL(18,4) | 음수 불가, 통화별 scale |
| WalletBalance | FR-002, FR-003 | wallet_balance | balance ≥ 0, @Version (Optimistic Lock) |
| LedgerEntry | FR-003 | ledger_entry | INSERT-only, balance_after 기록 필수 |
| PaymentTransaction | FR-101~105 | payment_transaction | 상태 머신, idempotency_key 중복 방지 |
| ExchangeTransaction | FR-201~204 | exchange_transaction | 단일 트랜잭션에서 EXCHANGE_OUT/IN 쌍 생성 |
| Member | FR-001 | member | 논리 삭제 (status=CLOSED) |
