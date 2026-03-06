# Decisions

- 배포 단위는 단일 Spring Boot 앱으로 유지 — 과제 범위에서 MSA는 오버엔지니어링이라 판단
- 현재 기수는 `app.current-cohort-number` 설정으로 외부화 — 기수 변경 시 코드 수정 불필요
- 소프트 삭제: 회원 탈퇴 → `WITHDRAWN`, 일정 삭제 → `CANCELLED` 상태 전이로 처리
- 보증금 차감/환급은 `DepositHistory`로 이력을 남김 — 잔액만 수정하는 방식은 추적이 안 돼서 배제
- QR 출석 검증 순서는 명세 기준으로 고정하고 테스트로 보장
- DTO 매핑은 record + 정적 팩토리로 수동 처리 — 과제 범위에서 MapStruct 같은 추가 의존성은 불필요하다 생각
- 서비스 단위 테스트는 Repository 전부 mock 처리 후 시나리오 단위로 작성
