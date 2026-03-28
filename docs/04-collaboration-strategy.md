# LemonPay 협업 전략

> 멀티통화 간편결제 시스템 - GitHub Projects 기반 협업 전략
> 버전: 1.0 | 최종 수정일: 2026-03-08 | 상태: 확정

---

## 1. GitHub Projects 활용법

### 1.1 프로젝트 보드 구조

GitHub Projects(User-level)를 단일 보드로 운영하여 BE/FE 이슈를 통합 추적합니다.

| 컬럼 | 설명 | 자동화 |
|------|------|--------|
| Backlog | 스프린트 미배정 이슈 | 이슈 생성 시 자동 배치 |
| Sprint N | 현재 스프린트 할당 이슈 | 마일스톤 기반 필터 |
| In Progress | 작업 중 | PR 생성 시 자동 이동 |
| In Review | 리뷰 대기 | PR 리뷰 요청 시 자동 이동 |
| Done | 완료 | PR 머지 시 자동 이동 |

### 1.2 이슈-PR-문서 연결

```
Issue (요구사항)
  ├── Branch: <type>/<이슈번호>-<설명>
  ├── PR: "Closes #이슈번호" → 머지 시 이슈 자동 닫힘
  ├── 코드 변경
  └── 문서 변경 (같은 PR에 포함, 또는 docs/ prefix 별도 이슈)
```

**Epic 관리**: Parent issue(예: #8 Sprint 1 BE)에 tasklist로 하위 이슈 연결.

### 1.3 라벨 체계

**레포별 공통:**
- 스프린트: `sprint-1`, `sprint-2`, `sprint-3`
- 타입: `type:feature`, `type:docs`, `type:test`, `type:infra`, `type:fix`
- 우선순위: `priority:high`, `priority:medium`, `priority:low`

**BE 전용 (DDD 도메인별):**
- `domain:wallet`, `domain:payment`, `domain:exchange`, `domain:ledger`, `domain:member`

**FE 전용:**
- `component`, `api-integration`, `store`

**공통:**
- `api-contract` - API 계약 변경 관련
- `docs-only` - 문서만 변경 (경량 리뷰)

### 1.4 이슈 네이밍 규칙

| 레포 | 형식 | 예시 |
|------|------|------|
| BE | `[BE] <기능 설명>` | `[BE] Ledger 기반 잔액 차감 로직 구현` |
| FE | `[FE] <기능 설명>` | `[FE] PaymentForm 컴포넌트 구현` |
| 연동 | `[FE] <API명> Mock->Real 전환 (Refs: BE#이슈번호)` | `[FE] 지갑 API Mock->Real 전환 (Refs: BE#15)` |

---

## 2. 문서 관리 전략

### 2.1 docs/ 디렉토리 구조

```
lemonpay-backend/docs/
├── 01-requirements.md              # 요구사항 명세서 (유저 스토리)
├── 02-erd.md                       # ERD (Mermaid 다이어그램)
├── 03-ubiquitous-language.md       # 유비쿼터스 언어 사전
├── 04-collaboration-strategy.md    # 협업 전략 (이 문서)
├── 05-branch-protection.md         # 브랜치 보호 전략
├── adr/                            # Architecture Decision Records
│   ├── 0001-ledger-based-balance.md
│   ├── 0002-optimistic-locking.md
│   ├── 0003-saga-pattern.md
│   └── 0004-circuit-breaker-fallback.md
└── api/
    └── API_CONTRACT.md             # BE API 스펙 (SpringDoc 보완용)

lemonpay-frontend/src/api/
└── API_CONTRACT.md                 # FE API 계약서 (Single Source of Truth)
```

### 2.2 ADR (Architecture Decision Record)

핀테크 회사에서는 설계 결정의 근거를 문서화하는 것이 감사(audit) 및 지식 공유 관점에서 필수입니다. LemonPay에서도 ADR을 도입합니다.

**ADR 템플릿:**
```markdown
# ADR-NNNN: <제목>

> 상태: Proposed | Accepted | Deprecated | Superseded by ADR-XXXX
> 작성일: YYYY-MM-DD

## 맥락 (Context)
어떤 문제를 해결해야 하는가?

## 결정 (Decision)
어떤 방식을 선택했는가?

## 대안 (Alternatives Considered)
검토한 다른 방안과 그 장단점.

## 결과 (Consequences)
이 결정으로 인한 긍정적/부정적 영향.
```

**ADR 대상 주제:**
| ADR | 주제 | 포트폴리오 어필 |
|-----|------|----------------|
| 0001 | Ledger 기반 잔액 vs 단순 UPDATE | 금융 정합성, 감사 추적 |
| 0002 | 낙관적 락 vs 비관적 락 | 동시성 전략 트레이드오프 |
| 0003 | Saga vs 2PC | 분산 트랜잭션 이해도 |
| 0004 | Circuit Breaker fallback | 장애 대응 전략 |

### 2.3 문서 리뷰 정책

- ERD, ADR, 요구사항 변경: **PR 리뷰 필수** (코드 변경만큼 중요)
- 오탈자, 포맷 수정: `docs-only` 라벨 → 리뷰 없이 머지 가능
- 코드와 문서가 함께 변경되면 같은 PR에 포함

---

## 3. 개발 방법론

### 3.1 Definition of Done

#### BE (백엔드)

| 항목 | 기준 |
|------|------|
| 빌드 | `./gradlew build` 성공 |
| 테스트 | 관련 단위 테스트 작성 + 기존 테스트 전체 통과 |
| 문서 | DB 변경 시 ERD 업데이트, 새 설계 결정 시 ADR 작성 |
| PR | 체크리스트 전항목 통과 |
| API | 엔드포인트 추가/변경 시 API_CONTRACT.md 반영 |

#### FE (프론트엔드)

| 항목 | 기준 |
|------|------|
| 동작 | Mock 모드에서 정상 동작 (`VITE_USE_MOCK=true`) |
| 계약 | Props/Emits가 API_CONTRACT.md와 일치 |
| 아키텍처 | Pinia store 경유 (컴포넌트에서 직접 API 호출 금지) |
| 포맷 | useMoney.js로 금액 포맷팅 |
| UI 상태 | 에러/로딩/빈 데이터 상태 처리 |
| 스크린샷 | PR에 동작 스크린샷 첨부 |

### 3.2 PR 리뷰 기준

#### BE PR 체크리스트 (기존 템플릿 + 핀테크 보강)

기본:
- [ ] 빌드 성공 (`./gradlew build`)
- [ ] 기존 테스트 통과
- [ ] 금액 연산에 BigDecimal 사용 (double/float 금지)
- [ ] 새 엔티티에 적절한 인덱스 설정
- [ ] 민감 정보 로깅 없음 (계좌번호, 잔액 등)
- [ ] API 응답에 Entity 직접 반환 금지

핀테크 특화:
- [ ] 도메인 로직이 도메인 계층에 위치 (Service/Controller에 비즈니스 로직 누수 없음)
- [ ] Ledger entry는 INSERT only (수정/삭제 로직 없음)
- [ ] 결제 API 멱등성 고려
- [ ] Value Object 불변성 유지

#### FE PR 리뷰 포인트

- 아키텍처 원칙 준수 (store 경유, composable 사용)
- API_CONTRACT.md와의 정합성
- 불필요한 복잡도 없는지

### 3.3 코드 품질 게이트

| 단계 | 도구 | 시점 |
|------|------|------|
| Sprint 1 | `./gradlew test` 통과 | 현재 (수동, CI 미연동) |
| Sprint 2 | JaCoCo 커버리지 리포트 (도메인 및 애플리케이션 계층 80% 목표) | 도입 예정 |
| Sprint 2+ | ArchUnit (DDD 계층 의존성 검증) | 검토 |
| Sprint 2+ | ESLint + Prettier (FE) | 도입 예정 |

---

## 4. BE-FE 협업 인터페이스

### 4.1 API 계약 관리

```
                  ┌──────────────────────┐
                  │  API_CONTRACT.md     │ ← Single Source of Truth (FE repo)
                  │  (FE: src/api/)      │
                  └──────┬───────────────┘
                         │ 참조
          ┌──────────────┼──────────────┐
          v              v              v
    mock.js         wallet.js      SpringDoc
   (Mock API)      (Real API)    (/v3/api-docs)
```

**핵심 원칙:**
- FE `src/api/API_CONTRACT.md`가 API 계약의 Single Source of Truth
- BE는 SpringDoc(OpenAPI 3)으로 `/v3/api-docs` 자동 생성하여 보완
- API 변경 시 PR 템플릿의 "API 변경" 섹션 필수 기재 (하위호환성 여부 포함)

### 4.2 API 계약 변경 프로세스

```
1. 변경 필요 발생
   └── Issue 생성 (라벨: api-contract)
       └── 변경 전/후 스펙 명시

2. BE 구현
   └── PR에 API 변경 섹션 기재
       └── 하위 호환성: 유지 / 깨짐 (깨지면 사유 기재)

3. FE 반영
   └── API_CONTRACT.md 업데이트
       └── mock.js → api/*.js → store → component 순서로 수정

4. 통합 검증
   └── api-integration-agent가 CORS, 인증, 응답 구조 테스트
```

### 4.3 Mock -> Real 전환 프로세스

1. BE가 엔드포인트 구현 완료 → BE Issue에 명시
2. FE에서 전환 전용 Issue 생성: `[FE] <API명> Mock->Real 전환 (Refs: BE#이슈번호)`
3. api-integration-agent가 real API 모듈 테스트 (CORS, 인증, 응답 구조)
4. `VITE_USE_MOCK=false`로 전환 후 E2E 확인
5. 리뷰 후 머지

### 4.4 브랜치 전략

BE/FE 모두 **GitHub Flow + develop 브랜치** 방식을 채택합니다. Git Flow의 release/hotfix는 토이 프로젝트에 과하므로 생략합니다.

```
main (보호 브랜치, 안정 릴리스)
  └── develop (개발 통합)
        ├── feature/<이슈번호>-<설명>     # 기능 개발
        ├── fix/<이슈번호>-<설명>         # 버그 수정
        ├── docs/<설명>                  # 문서 단독 변경
        └── api/<이슈번호>-<계약변경>     # API 계약 변경 (FE)
```

**브랜치 네이밍 예시:**

| 레포 | 예시 |
|------|------|
| BE | `feature/2-ddd-package-structure` |
| BE | `feature/3-value-objects` |
| BE | `docs/erd-sprint1` |
| FE | `feat/fe-12-payment-form` |
| FE | `api/fe-15-wallet-mock-to-real` |

**머지 규칙:**
- feature/fix → develop: PR 리뷰 후 머지
- develop → main: 스프린트 완료 시 머지

---

## 5. 포트폴리오 관점 요약

이 협업 전략에서 면접관에게 어필할 수 있는 포인트:

| 항목 | 어필 포인트 |
|------|------------|
| ADR 도입 | "설계 결정의 근거를 문서화하는 습관" -- 시니어 역량 |
| 도메인별 라벨 | "DDD를 코드뿐 아니라 프로젝트 관리에도 일관 적용" |
| PR 체크리스트 핀테크 특화 | "금융 도메인의 보안/정합성을 개발 프로세스에 내재화" |
| API 계약 관리 | "FE-BE 병렬 개발을 위한 Contract-First 접근" |
| Mock->Real 전환 추적 | "체계적 통합 프로세스로 품질 보장" |

---

*PM Agent | LemonPay Project | 2026-03-08*
*BE/FE Lead 의견 통합 기반 수립*
