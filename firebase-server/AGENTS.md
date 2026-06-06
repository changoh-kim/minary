# firebase-server/AGENTS.md

> **BLUF**
> Minary의 백엔드 인프라(Cloud Functions, Security Rules). 과금 폭탄 방지(Kill Switch)와 개인정보(PII) 보호를 최우선으로 설계된 **최후의 방어선**이다.

## 1. Cloud Functions 작성 규칙
- **Region**: 모든 Function은 `asia-northeast3`를 기본 리전으로 사용한다. 새 Function 추가 시 리전 누락을 금지한다.
- **Trigger Loop 방지**: 트리거에서 update 수행 전 변경 여부를 반드시 검증한다 (`if (before == after) { return }`). `lastModifiedAt`만 변경되는 무한 트리거 루프를 철저히 방지한다.
- **검증**: `request.auth`, App Check 토큰, 필수 데이터를 최우선 검증한다. 모든 함수는 `Timeout`, `Retry`, `Idempotency`를 고려한다.

## 2. 보안 및 경로 동기화
- **Kill Switch**: `assertServiceAvailable()`을 통해 서비스 상태를 체크하여 예산 초과 시 즉시 차단한다.
- **경로 동기화**: Firestore/Storage 경로 변경 시 클라이언트 측 `FirebaseFirestoreProvider`, `FirebaseStorageProvider` 등의 경로 상수와 반드시 일치시켜야 한다.
- **Secrets**: `defineSecret`을 사용하여 Slack Webhook URL 등 민감 정보를 관리한다.

## 3. Firestore & Storage Security Rules
- **소유권**: 유저 데이터 경로는 반드시 `request.auth.uid == userId`를 충족할 때만 접근을 허용한다.
- **제한**: 파일 업로드 시 사이즈(`< 5MB`) 및 Content-Type(`image/.*`) 검증을 엄격히 수행한다.
- **Batch Deletion**: 계정 탈퇴 시 `deleteAccountAndUserData`에서 Batch Pagination 전략을 사용하여 Timeout과 비용 폭탄을 방지한다.

## 4. 로깅 및 비용 방어
- **PII**: 이메일, 일기 본문 등 개인정보를 로그에 남기는 행위를 절대 금지한다.
- **Billing**: `handleBudgetAlert`을 통해 예산 도달 시 단계별(알림 -> Kill Switch -> Billing Disconnect) 조치를 수행한다.