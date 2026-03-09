# 브랜치 보호 전략

> LemonPay Backend — GitHub 브랜치 보호 규칙
> 버전: 1.0 | 최종 수정일: 2026-03-09 | 상태: 확정

---

## 1. main 브랜치 보호 규칙

### 적용 규칙 요약

| 규칙 | 설정값 | 이유 |
|------|--------|------|
| PR 필수 (리뷰 1명) | ✅ 활성화 | 리뷰 없이 머지 불가. 빅테크/핀테크 표준 |
| Stale 리뷰 무효화 | ✅ 활성화 | 새 커밋 push 시 이전 approve 무효. 조용한 우회 방지 |
| Status Check (CI) | ❌ Sprint 1 미적용 | CI 미구성. Sprint 2에서 build + JaCoCo 추가 예정 |
| Force push 금지 | ✅ 적용 | 커밋 히스토리 보호. 금융 감사 추적(audit trail) 요건 |
| 브랜치 삭제 금지 | ✅ 적용 | main 실수 삭제 방지 |
| 관리자도 예외 없음 (enforce_admins) | ✅ 적용 | 포트폴리오 신뢰성. PCI-DSS, SOC 2 컴플라이언스 시그널 |

---

## 2. GitHub UI에서 적용하는 방법

### 2.1 경로

```
GitHub → lemonpay-backend 레포
  → Settings → Branches → Add branch ruleset (또는 Add branch protection rule)
```

> **직접 링크**: https://github.com/junoade/lemonpay-backend/settings/branches

---

### 2.2 단계별 적용 가이드

**① "Add branch protection rule" 클릭**

**② Branch name pattern 입력**
```
main
```

**③ 아래 항목을 순서대로 체크**

| 항목 | 설정 |
|------|------|
| ✅ Require a pull request before merging | 체크 |
| &nbsp;&nbsp;ㄴ Required number of approvals before merging | `1` |
| &nbsp;&nbsp;ㄴ Dismiss stale pull request approvals when new commits are pushed | ✅ 체크 |
| ✅ Do not allow bypassing the above settings | 체크 (= enforce_admins) |
| ✅ Allow force pushes | ❌ **체크 해제** (금지) |
| ✅ Allow deletions | ❌ **체크 해제** (금지) |

> `Require status checks` 는 Sprint 1에서 **건너뜁니다**. CI가 없으므로 체크하면 머지 자체가 불가능해집니다.

**④ "Create" 클릭 → 완료**

---

### 2.3 적용 확인 방법

```
Settings → Branches 화면에서
"main" 줄에 보호 규칙 아이콘이 보이면 정상 적용
```

또는 직접 push 시도로 확인:
```bash
git checkout main
git push origin main  # 거절되면 보호 규칙 정상 적용
# Expected: GH006: Protected branch update failed
```

---

## 3. dev 브랜치 권장 규칙 (Sprint 2부터 적용)

솔로 개발 특성상 dev는 리뷰 없이 머지 가능하도록 설정합니다.

| 규칙 | 설정값 |
|------|--------|
| Required approvals | 0 (PR 형식만, 블로킹 없음) |
| Dismiss stale reviews | ✅ 활성화 |
| Force push 금지 | ✅ 적용 |
| 브랜치 삭제 금지 | ✅ 적용 |
| enforce_admins | ❌ 비적용 (긴급 fix 대응 위해) |

---

## 4. Sprint별 강화 로드맵

| Sprint | main | dev |
|--------|------|-----|
| **Sprint 1 (현재)** | PR 리뷰 필수, `./gradlew test` 수동 실행 (GitHub Actions 미연동) | 보호 없음 |
| **Sprint 2** | PR + CI (`build` + JaCoCo 도메인·애플리케이션 계층 80%) | PR (리뷰 0) + CI |
| **Sprint 3** | PR + CI + 커버리지 게이트 + ArchUnit | Sprint 2와 동일 |

> **Sprint 1 참고**: `./gradlew test` 통과는 PR 체크리스트 항목으로 수동 확인합니다.
> GitHub Actions 미연동 상태이므로 branch protection의 Status Check는 비활성화 상태입니다.

### Sprint 2 업그레이드 시 추가할 항목
```
Settings → Branches → main 규칙 편집
  → Require status checks to pass before merging ✅
    → "build" 체크 추가 (GitHub Actions job 이름)
  → Require branches to be up to date before merging ✅
```

---

## 5. 왜 솔로 프로젝트에도 브랜치 보호가 필요한가?

| 관점 | 이유 |
|------|------|
| **핀테크 컴플라이언스** | PCI-DSS, SOC 2는 "프로덕션 브랜치에 대한 모든 변경에 리뷰 기록 필수"를 요구 |
| **감사 추적** | force push 없음 = 히스토리 불변. 원장(Ledger)처럼 git도 append-only 원칙 |
| **포트폴리오** | "혼자 해도 이런 규칙을 지킨다"는 것이 시니어에게 보이는 시그널 |
| **enforce_admins** | 관리자도 예외 없음 = 가장 강한 보호. 컴플라이언스 감사관이 주목하는 설정 |

---

*DevOps 에이전트 (Claude Sonnet 4.6) | 2026-03-09*
