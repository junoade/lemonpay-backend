# 실험: JPA 엔티티를 Application 경계 밖으로 노출했을 때 발생하는 문제

## 배경

`WalletQueryResult`가 `List<WalletBalance>`를 직접 노출할 때 발생할 수 있는 문제를 검증한다.

---

## 실험 1: LazyInitializationException

### 조건
- `WalletBalance`의 `wallet` 필드: `@ManyToOne(fetch = FetchType.LAZY)`
- Controller에서 `WalletBalance.getWallet()` 접근 시도

### 재현 코드
```java
// WalletV1Controller.java
@GetMapping("/{walletId}/balances")
public ResponseEntity<WalletDto.BalancesResponse> getBalances(@PathVariable UUID walletId) {
    WalletQueryResult result = walletQueryService.getWalletBalances(walletId);

    // 트랜잭션 종료 후 Lazy 필드 접근 시도
    result.walletBalances().forEach(wb -> {
        String walletName = wb.getWallet().getName(); // ← 여기서 터짐
    });
    ...
}
```

### 예상 결과
```
org.hibernate.LazyInitializationException:
failed to lazily initialize a collection or proxy:
could not initialize proxy - no Session
```

### 원인
`WalletQueryService.getWalletBalances()` 트랜잭션이 종료된 후 Controller에서
Lazy 프록시에 접근하면 영속성 컨텍스트가 이미 닫혀있어 초기화 불가.

### 실험 결과: 예외가 안 터지는 경우 - OSIV

Spring Boot 기본값이 `spring.jpa.open-in-view=true`이면 예외가 발생하지 않는다.
OSIV는 HTTP 요청 시작부터 응답까지 Hibernate 세션을 열어두기 때문에
트랜잭션이 끝나도 Lazy 로딩이 가능하다.

**앱 시작 시 아래 경고가 로그에 출력됨:**
```
WARN o.s.b.a.orm.jpa.JpaBaseConfiguration :
Spring recommends disabling spring.jpa.open-in-view as it initializes a
lazy loaded JPA entity from outside a transaction.
```

**OSIV의 문제점:**
- DB 커넥션을 요청 전체 동안 점유 → 트래픽 많을 때 커넥션 풀 고갈 위험
- 트랜잭션 밖에서 Lazy 로딩이 되는 의도치 않은 동작
- 실무에서는 OSIV 끄고 명시적 fetch join 사용 권장

**재현 방법 (OSIV 비활성화):**
```yaml
# application-local.yml
spring:
  jpa:
    open-in-view: false
```
위 설정 후 재시작하면 `LazyInitializationException` 발생.

---

## 실험 2: 순환 참조 무한루프 (JSON 직렬화)

### 조건
- `WalletBalance` → `Wallet` (ManyToOne)
- `Wallet` → `List<WalletBalance>` (OneToMany)
- Jackson이 `WalletBalance`를 직렬화 시도

### 재현 코드
```java
// WalletQueryResult에서 WalletBalance를 그대로 응답에 포함
public record BalancesResponse(
    UUID walletId,
    List<WalletBalance> balances  // ← 엔티티 직접 노출
) {}
```

### 예상 결과
```
com.fasterxml.jackson.databind.exc.InvalidDefinitionException:
No serializer found for class org.hibernate.proxy.HibernateProxy
또는 StackOverflowError (순환 참조)
```

### 원인
`WalletBalance` → `Wallet` → `List<WalletBalance>` → `Wallet` → ... 무한 순환.

---

## 실험 3: 의도치 않은 내부 필드 노출

### 조건
- `WalletBalance`에 `@Version private Long version` 필드 존재
- API 응답에 그대로 포함

### 예상 결과
```json
{
  "currency": "KRW",
  "balance": 50000.0000,
  "version": 3,          ← 내부 낙관적 락 정보 노출
  "wallet": { ... }      ← 연관 엔티티 전체 노출
}
```

### 원인
DTO 변환 없이 엔티티를 직렬화하면 모든 필드가 노출된다.
클라이언트가 알 필요 없는 내부 구현 정보까지 포함.

---

## 해결책

`WalletQueryResult`에서 엔티티를 VO(Money)로 변환 후 노출:

```java
public record WalletQueryResult(
        UUID walletId,
        List<Money> balances  // WalletBalance → Money 변환
) {
    public static WalletQueryResult of(UUID walletId, List<WalletBalance> walletBalances) {
        return new WalletQueryResult(
                walletId,
                walletBalances.stream().map(WalletBalance::toMoney).toList()
        );
    }
}
```

- Lazy 로딩 문제 없음 (`Money`는 단순 record)
- 순환 참조 없음
- 노출 필드 제어 가능

---

## 결론

| 문제 | 발생 조건 | 심각도 |
|------|----------|--------|
| LazyInitializationException | 트랜잭션 밖에서 Lazy 필드 접근 (OSIV=false 조건) | 런타임 에러 |
| 순환 참조 StackOverflow | 양방향 연관관계 직렬화 | 런타임 에러 |
| 내부 필드 노출 | DTO 변환 없이 직렬화 | 보안/설계 문제 |

JPA 엔티티는 영속성 컨텍스트 안에서만 다루고,
Application 경계 밖으로는 VO 또는 전용 Result 객체로 변환해서 전달한다.
