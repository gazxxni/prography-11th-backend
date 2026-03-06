# Prography 11th Backend

Prography 11기 백엔드 과제용 출결 관리 시스템이다. Spring Boot 3.x, JPA, H2, Swagger UI 기반으로 구현했다.

## Stack
- Java 17
- Spring Boot 3.2.x
- Spring Web, Validation, Data JPA, Security
- H2 in-memory database
- springdoc-openapi
- JUnit 5, Mockito
- Gradle Kotlin DSL

## Core Features
- 로그인
- 회원 등록, 조회, 수정, 탈퇴
- 기수/파트/팀 조회
- 일정 생성, 조회, 수정, 취소
- QR 생성 및 갱신
- QR 출석 체크
- 관리자 출결 등록/수정
- 보증금 차감 및 환급 이력 관리

## Package Structure
- `com.prography.attendance.global`: 공통 설정, 응답, 예외, 시드 데이터
- `com.prography.attendance.domain.auth`: 로그인
- `com.prography.attendance.domain.member`: 회원
- `com.prography.attendance.domain.cohort`: 기수, 파트, 팀, 기수 회원
- `com.prography.attendance.domain.session`: 일정, QR
- `com.prography.attendance.domain.attendance`: 출결
- `com.prography.attendance.domain.deposit`: 보증금 이력

## 빌드 및 실행

### 요구사항
- JDK 17+

### 실행
```bash
./gradlew bootRun
```

### 테스트
```bash
./gradlew test
```

## API Docs
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:prography`
  - Username: `sa`
  - Password: 빈 값

## Seed Data
- Cohort: 10기, 11기
- Part: 각 기수별 `SERVER`, `WEB`, `iOS`, `ANDROID`, `DESIGN`
- Team: 11기 `Team A`, `Team B`, `Team C`
- Admin Member
  - `loginId`: `admin`
  - `password`: `admin1234`

## Configuration
`src/main/resources/application.yml`

- H2 in-memory DB
- `ddl-auto: create`
- `app.current-cohort-number: 11`
- timezone: `Asia/Seoul`

## Documents
- `docs/ERD.md`
- `docs/SYSTEM_ARCHITECTURE.md`
- `docs/DECISIONS.md`
- `docs/AI_USAGE.md`

## Notes
- 모든 API는 `/api/v1` 기준으로 노출된다.
- 공통 응답 형식은 `ApiResponse<T>`를 사용한다.
- 인증 토큰 발급은 구현하지 않고 로그인 시 회원 정보만 반환한다.
- 보안 설정은 과제 명세에 맞춰 전체 요청 `permitAll`로 두었다.
