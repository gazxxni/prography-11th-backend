# AI Usage

## Tool
- Codex 기반 에이전트를 사용해 초기 스캐폴딩, API 구현, 테스트 작성, 문서화를 진행했다.

## Where AI Helped
- 요구사항 명세를 코드 구조로 분해
- 엔티티, DTO, 서비스, 컨트롤러, 예외 처리, 시드 데이터 초안 생성
- 서비스 레이어 단위 테스트 시나리오 작성
- 문서 초안 작성

## Human Review Points
- API 응답 필드와 상태 코드가 프론트 기대값과 정확히 맞는지 확인 필요
- 현재 기수 중심 조회 규칙이 실제 운영 의도와 일치하는지 확인 필요
- API 22의 `sessionId` 사용 의미가 명세상 다소 모호해, 현재는 세션 존재 검증 후 현재 기수 멤버 전체 요약을 반환하도록 구현함
- 실서비스 배포 전에는 H2 대신 운영 DB, 인증 체계, 로깅/모니터링, 트랜잭션 경계 재검토 필요

## Verification Performed
- 애플리케이션 컨텍스트 기동 테스트
- AuthService, MemberService, SessionService, QrCodeService, AttendanceService 단위 테스트
- 검증 명령: `./gradlew test`
