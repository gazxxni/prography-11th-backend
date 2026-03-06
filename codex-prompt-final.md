# Codex 프롬프트: Prography 11기 백엔드 과제 (출결관리 시스템)

## 역할

너는 Spring Boot 백엔드 시니어 개발자야. 아래 요구사항을 **빠짐없이, 컴파일·실행 가능한 상태**로 구현해. 주석은 달지 마.

---

## 프로젝트 메타

- **레포명**: `prography-11th-backend`
- **JDK**: 17
- **Framework**: Spring Boot 3.x (spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation)
- **DB**: H2 (인메모리, 콘솔 활성화)
- **API 문서**: springdoc-openapi (Swagger UI)
- **테스트**: JUnit 5 + Mockito
- **비밀번호 해싱**: BCryptPasswordEncoder (cost factor 12). spring-boot-starter-security 사용하되 SecurityFilterChain에서 모든 요청 permitAll, csrf disable, h2-console frameOptions disable
- **빌드**: Gradle (Kotlin DSL)
- **Base URL**: `/api/v1`
- **타임존**: Asia/Seoul

---

## 공통 응답 형식

### 성공 응답
```json
{
  "success": true,
  "data": { ... },
  "error": null
}
```

### 실패 응답
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지"
  }
}
```

### 페이징 응답 (data 안에)
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 10,
  "totalElements": 50,
  "totalPages": 5
}
```

→ `ApiResponse<T>` 래퍼 클래스를 만들어 모든 컨트롤러에서 사용.

---

## Enum 정의

| Enum | 값 |
|------|-----|
| MemberStatus | ACTIVE, INACTIVE, WITHDRAWN |
| MemberRole | MEMBER, ADMIN |
| SessionStatus | SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED |
| AttendanceStatus | PRESENT, ABSENT, LATE, EXCUSED |
| DepositType | INITIAL, PENALTY, REFUND |

---

## 에러 코드 전체 목록

| 코드 | HTTP Status | 메시지 |
|------|-------------|--------|
| INVALID_INPUT | 400 | 입력값이 올바르지 않습니다 |
| INTERNAL_ERROR | 500 | 서버 내부 오류가 발생했습니다 |
| LOGIN_FAILED | 401 | 로그인 아이디 또는 비밀번호가 올바르지 않습니다 |
| MEMBER_WITHDRAWN | 403 | 탈퇴한 회원입니다 |
| MEMBER_NOT_FOUND | 404 | 회원을 찾을 수 없습니다 |
| DUPLICATE_LOGIN_ID | 409 | 이미 사용 중인 로그인 아이디입니다 |
| MEMBER_ALREADY_WITHDRAWN | 400 | 이미 탈퇴한 회원입니다 |
| COHORT_NOT_FOUND | 404 | 기수를 찾을 수 없습니다 |
| PART_NOT_FOUND | 404 | 파트를 찾을 수 없습니다 |
| TEAM_NOT_FOUND | 404 | 팀을 찾을 수 없습니다 |
| COHORT_MEMBER_NOT_FOUND | 404 | 기수 회원 정보를 찾을 수 없습니다 |
| SESSION_NOT_FOUND | 404 | 일정을 찾을 수 없습니다 |
| SESSION_ALREADY_CANCELLED | 400 | 이미 취소된 일정입니다 |
| SESSION_NOT_IN_PROGRESS | 400 | 진행 중인 일정이 아닙니다 |
| QR_NOT_FOUND | 404 | QR 코드를 찾을 수 없습니다 |
| QR_INVALID | 400 | 유효하지 않은 QR 코드입니다 |
| QR_EXPIRED | 400 | 만료된 QR 코드입니다 |
| QR_ALREADY_ACTIVE | 409 | 이미 활성화된 QR 코드가 있습니다 |
| ATTENDANCE_NOT_FOUND | 404 | 출결 기록을 찾을 수 없습니다 |
| ATTENDANCE_ALREADY_CHECKED | 409 | 이미 출결 체크가 완료되었습니다 |
| EXCUSE_LIMIT_EXCEEDED | 400 | 공결 횟수를 초과했습니다 (최대 3회) |
| DEPOSIT_INSUFFICIENT | 400 | 보증금 잔액이 부족합니다 |

---

## 패키지 구조

```
com.prography.attendance
├── global/
│   ├── config/          # SecurityConfig, ClockConfig, SwaggerConfig
│   ├── error/           # ErrorCode(enum), BusinessException, GlobalExceptionHandler
│   ├── common/          # BaseTimeEntity, ApiResponse<T>
│   └── seed/            # DataInitializer (ApplicationRunner)
├── domain/
│   ├── auth/            # controller, dto, service
│   ├── member/          # entity(Member), repository, controller, dto, service
│   ├── cohort/          # entity(Cohort, Part, Team, CohortMember), repository, controller, dto, service
│   ├── session/         # entity(Session, QrCode), repository, controller, dto, service
│   ├── attendance/      # entity(Attendance), repository, controller, dto, service
│   └── deposit/         # entity(DepositHistory), repository, service
└── AttendanceApplication.java
```

---

## 엔티티 설계

### Member
| 필드 | 타입 | 제약 |
|------|------|------|
| id | Long (PK, auto) | |
| loginId | String | unique, not null |
| password | String | BCrypt hashed, not null |
| name | String | not null |
| phone | String | not null |
| role | Enum(ADMIN, MEMBER) | not null |
| status | Enum(ACTIVE, INACTIVE, WITHDRAWN) | default ACTIVE |
| createdAt, updatedAt | Instant | @CreatedDate, @LastModifiedDate |

### Cohort
| 필드 | 타입 |
|------|------|
| id | Long (PK, auto) |
| generation | Integer (unique) |
| name | String (예: "10기", "11기") |
| createdAt | Instant |

### Part
| 필드 | 타입 |
|------|------|
| id | Long (PK, auto) |
| cohort | ManyToOne → Cohort |
| name | String (SERVER, WEB, iOS, ANDROID, DESIGN) |

- unique: (cohort, name)

### Team
| 필드 | 타입 |
|------|------|
| id | Long (PK, auto) |
| cohort | ManyToOne → Cohort |
| name | String |

### CohortMember (핵심)
| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK, auto) | |
| cohort | ManyToOne → Cohort | |
| member | ManyToOne → Member | |
| part | ManyToOne → Part (nullable) | |
| team | ManyToOne → Team (nullable) | |
| deposit | Integer | 초기 100,000 |
| excuseCount | Integer | 초기 0, 기수당 max 3 |

- unique: (cohort, member)

### Session
| 필드 | 타입 |
|------|------|
| id | Long (PK, auto) |
| cohort | ManyToOne → Cohort |
| title | String |
| date | LocalDate |
| time | LocalTime |
| location | String |
| status | Enum(SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED) |
| createdAt, updatedAt | Instant |

### QrCode
| 필드 | 타입 |
|------|------|
| id | Long (PK, auto) |
| session | ManyToOne → Session |
| hashValue | String (UUID) |
| createdAt | Instant |
| expiresAt | Instant (생성 + 24h) |

### Attendance
| 필드 | 타입 |
|------|------|
| id | Long (PK, auto) |
| session | ManyToOne → Session |
| cohortMember | ManyToOne → CohortMember |
| status | Enum(PRESENT, LATE, ABSENT, EXCUSED) |
| lateMinutes | Integer (nullable) |
| penaltyAmount | Integer |
| reason | String (nullable) |
| checkedInAt | Instant (nullable) |
| createdAt, updatedAt | Instant |

- unique: (session, cohortMember)

### DepositHistory
| 필드 | 타입 |
|------|------|
| id | Long (PK, auto) |
| cohortMember | ManyToOne → CohortMember |
| type | Enum(INITIAL, PENALTY, REFUND) |
| amount | Integer (INITIAL/REFUND=양수, PENALTY=음수) |
| balanceAfter | Integer |
| attendance | ManyToOne → Attendance (nullable) |
| description | String (nullable) |
| createdAt | Instant |

---

## 시드 데이터 (DataInitializer - ApplicationRunner)

서버 시작 시 자동 삽입:

1. **기수**: id=1 generation=10 name="10기", id=2 generation=11 name="11기"
2. **파트**: 기수별 SERVER, WEB, iOS, ANDROID, DESIGN (총 10개)
3. **팀**: 11기에 Team A, Team B, Team C (총 3개)
4. **관리자 Member**: loginId=`admin`, password=`admin1234`(BCrypt 해싱), name="관리자", phone="010-0000-0000", role=ADMIN, status=ACTIVE
5. **관리자 CohortMember**: 11기 소속, SERVER 파트, 팀 없음, deposit=100,000
6. **관리자 보증금 이력**: type=INITIAL, amount=100000, balanceAfter=100000, description="초기 보증금"

---

## API 25개 상세 명세

### API 1: POST /api/v1/auth/login (로그인)

**Request Body:**
```json
{ "loginId": "admin", "password": "admin1234" }
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "id": 1, "loginId": "admin", "name": "관리자", "phone": "010-0000-0000",
    "status": "ACTIVE", "role": "ADMIN",
    "createdAt": "2026-02-14T00:00:00Z", "updatedAt": "2026-02-14T00:00:00Z"
  },
  "error": null
}
```

**비즈니스 규칙:**
1. loginId로 회원 조회 → 없으면 LOGIN_FAILED
2. BCrypt 비밀번호 검증 → 불일치 시 LOGIN_FAILED
3. 회원 상태 WITHDRAWN이면 MEMBER_WITHDRAWN
4. 토큰 발급 없음 — 회원 정보만 반환

---

### API 2: GET /api/v1/members/{id} (회원 조회)

**Response 200 data:**
```json
{ "id", "loginId", "name", "phone", "status", "role", "createdAt", "updatedAt" }
```

**Error:** MEMBER_NOT_FOUND(404)

---

### API 3: POST /api/v1/admin/members (회원 등록)

**Request Body:**
```json
{
  "loginId": "user1", "password": "password123", "name": "홍길동",
  "phone": "010-1234-5678", "cohortId": 2, "partId": 6, "teamId": 1
}
```
- partId: 선택, teamId: 선택

**Response 201 data:**
```json
{
  "id", "loginId", "name", "phone", "status": "ACTIVE", "role": "MEMBER",
  "generation": 11, "partName": "SERVER", "teamName": "Team A",
  "createdAt", "updatedAt"
}
```

**비즈니스 규칙:**
1. loginId 중복 → DUPLICATE_LOGIN_ID
2. cohortId/partId/teamId 존재 검증
3. BCrypt 해싱 (cost factor 12)
4. Member 생성 (status=ACTIVE, role=MEMBER)
5. CohortMember 생성 (deposit=100,000)
6. DepositHistory 생성 (type=INITIAL, amount=100000)

---

### API 4: GET /api/v1/admin/members (회원 대시보드)

**Query Parameters:**
| 파라미터 | 기본값 | 설명 |
|----------|--------|------|
| page | 0 | 페이지 번호 |
| size | 10 | 페이지 크기 |
| searchType | - | name, loginId, phone |
| searchValue | - | 검색어 |
| generation | - | 기수 필터 |
| partName | - | 파트명 필터 |
| teamName | - | 팀명 필터 |
| status | - | ACTIVE, INACTIVE, WITHDRAWN |

**Response 200 data (페이징):**
```json
{
  "content": [{
    "id", "loginId", "name", "phone", "status", "role",
    "generation", "partName", "teamName", "deposit",
    "createdAt", "updatedAt"
  }],
  "page", "size", "totalElements", "totalPages"
}
```

---

### API 5: GET /api/v1/admin/members/{id} (회원 상세)

**Response 200 data:**
```json
{
  "id", "loginId", "name", "phone", "status", "role",
  "generation", "partName", "teamName",
  "createdAt", "updatedAt"
}
```
- generation/partName/teamName: CohortMember 없으면 null

---

### API 6: PUT /api/v1/admin/members/{id} (회원 수정)

**Request Body (모든 필드 optional):**
```json
{ "name", "phone", "cohortId", "partId", "teamId" }
```

**비즈니스 규칙:**
1. name, phone 전달 시 직접 수정
2. cohortId 전달 시: 해당 기수의 CohortMember가 있으면 partId/teamId 업데이트, 없으면 새 CohortMember 생성

**Response:** API 5와 동일 형식

---

### API 7: DELETE /api/v1/admin/members/{id} (회원 탈퇴)

Soft-delete: status → WITHDRAWN

**Response 200 data:**
```json
{ "id", "loginId", "name", "status": "WITHDRAWN", "updatedAt" }
```

**Error:** MEMBER_NOT_FOUND, MEMBER_ALREADY_WITHDRAWN

---

### API 8: GET /api/v1/admin/cohorts (기수 목록)

**Response 200 data (배열):**
```json
[{ "id", "generation", "name", "createdAt" }]
```

---

### API 9: GET /api/v1/admin/cohorts/{cohortId} (기수 상세)

**Response 200 data:**
```json
{
  "id", "generation", "name",
  "parts": [{ "id", "name" }],
  "teams": [{ "id", "name" }],
  "createdAt"
}
```

---

### API 10: GET /api/v1/sessions (일정 목록 - 회원용)

CANCELLED 제외, 현재 기수(11기)만

**Response 200 data (배열):**
```json
[{
  "id", "title", "date", "time", "location", "status",
  "createdAt", "updatedAt"
}]
```

---

### API 11: GET /api/v1/admin/sessions (일정 목록 - 관리자)

**Query Parameters:** dateFrom, dateTo (LocalDate), status (SessionStatus) — 모두 선택

CANCELLED 포함 모든 상태

**Response 200 data (배열):**
```json
[{
  "id", "cohortId", "title", "date", "time", "location", "status",
  "attendanceSummary": { "present", "absent", "late", "excused", "total" },
  "qrActive": true,
  "createdAt", "updatedAt"
}]
```

---

### API 12: POST /api/v1/admin/sessions (일정 생성)

**Request Body:**
```json
{ "title": "정기 모임", "date": "2026-03-01", "time": "14:00", "location": "강남" }
```

초기 상태: SCHEDULED, QR 자동 생성 (UUID, 24시간)

**Response 201 data:** API 11의 개별 항목과 동일

---

### API 13: PUT /api/v1/admin/sessions/{id} (일정 수정)

**Request Body (모든 필드 optional):**
```json
{ "title", "date", "time", "location", "status" }
```

**Error:** SESSION_NOT_FOUND, SESSION_ALREADY_CANCELLED

**Response 200 data:** API 11의 개별 항목과 동일

---

### API 14: DELETE /api/v1/admin/sessions/{id} (일정 삭제)

Soft-delete: status → CANCELLED

**Error:** SESSION_NOT_FOUND, SESSION_ALREADY_CANCELLED

**Response 200 data:** API 11의 개별 항목과 동일

---

### API 15: POST /api/v1/admin/sessions/{sessionId}/qrcodes (QR 생성)

Request Body 없음

**Response 201 data:**
```json
{ "id", "sessionId", "hashValue": "UUID", "createdAt", "expiresAt" }
```

**비즈니스 규칙:**
- 해당 일정에 활성(expiresAt > now) QR이 이미 있으면 → QR_ALREADY_ACTIVE
- 없으면 새 QR 생성

---

### API 16: PUT /api/v1/admin/qrcodes/{qrCodeId} (QR 갱신)

Request Body 없음

**비즈니스 규칙:**
1. 기존 QR의 expiresAt = now (즉시 만료)
2. 동일 sessionId로 새 QR 생성

**Response 200 data:** 새로 생성된 QR 정보 (API 15와 동일 형식)

---

### API 17: POST /api/v1/attendances (QR 출석 체크) [가산점]

**Request Body:**
```json
{ "hashValue": "UUID", "memberId": 1 }
```

**검증 순서 (반드시 이 순서):**
1. QR hashValue → DB에 없으면 QR_INVALID
2. QR 만료 → expiresAt < now이면 QR_EXPIRED
3. Session 상태 → IN_PROGRESS 아니면 SESSION_NOT_IN_PROGRESS
4. Member 존재 → MEMBER_NOT_FOUND
5. Member 탈퇴 → MEMBER_WITHDRAWN
6. 중복 출결 (sessionId + memberId) → ATTENDANCE_ALREADY_CHECKED
7. CohortMember 존재 (현재 기수) → COHORT_MEMBER_NOT_FOUND

**지각 판정 (Asia/Seoul):**
- `session.date + session.time` vs 현재 시각
- 현재 > 일정시각 → LATE (lateMinutes = 차이분)
- 현재 <= 일정시각 → PRESENT

**패널티:**
- PRESENT → 0원
- LATE → min(lateMinutes × 500, 10000)원

**후처리:**
- Attendance 저장 (checkedInAt = now)
- 패널티 > 0이면: 보증금 잔액 확인 → 부족하면 DEPOSIT_INSUFFICIENT → 충분하면 차감 + DepositHistory(PENALTY)

**Response 201 data:**
```json
{
  "id", "sessionId", "memberId", "status", "lateMinutes",
  "penaltyAmount", "reason": null, "checkedInAt",
  "createdAt", "updatedAt"
}
```

---

### API 18: GET /api/v1/attendances (내 출결 기록) [가산점]

**Query:** memberId (필수)

**Response 200 data (배열):**
```json
[{
  "id", "sessionId", "sessionTitle", "status", "lateMinutes",
  "penaltyAmount", "reason", "checkedInAt", "createdAt"
}]
```

---

### API 19: GET /api/v1/members/{memberId}/attendance-summary [가산점]

**Response 200 data:**
```json
{
  "memberId", "present", "absent", "late", "excused",
  "totalPenalty", "deposit"
}
```
- totalPenalty = 전체 penaltyAmount 합계
- deposit = CohortMember.deposit (없으면 null)

---

### API 20: POST /api/v1/admin/attendances (출결 등록) [가산점]

**Request Body:**
```json
{
  "sessionId": 1, "memberId": 1, "status": "ABSENT",
  "lateMinutes": null, "reason": "무단 결석"
}
```

**비즈니스 규칙:**
1. 일정/회원 존재 검증
2. 중복 출결 → ATTENDANCE_ALREADY_CHECKED
3. CohortMember 존재 확인
4. EXCUSED → excuseCount < 3 검증 → excuseCount++
5. 패널티 계산: PRESENT=0, ABSENT=10000, LATE=min(lateMinutes×500, 10000), EXCUSED=0
6. 패널티 > 0 → 보증금 차감 + DepositHistory(PENALTY)

**Response 201 data:**
```json
{
  "id", "sessionId", "memberId", "status", "lateMinutes",
  "penaltyAmount", "reason", "checkedInAt": null,
  "createdAt", "updatedAt"
}
```

---

### API 21: PUT /api/v1/admin/attendances/{id} (출결 수정) [가산점]

**Request Body:**
```json
{ "status": "EXCUSED", "lateMinutes": null, "reason": "병가" }
```

**패널티 diff 계산:**
```
oldPenalty = 기존 penaltyAmount
newPenalty = calculatePenalty(newStatus, newLateMinutes)
diff = newPenalty - oldPenalty
```

**EXCUSED 전환:**
| 전환 | 동작 |
|------|------|
| 다른 상태 → EXCUSED | excuseCount < 3 검증 → excuseCount++ |
| EXCUSED → 다른 상태 | excuseCount-- (min 0) |
| EXCUSED → EXCUSED | 변동 없음 |

**보증금 자동 조정:**
| 조건 | 동작 |
|------|------|
| diff > 0 | 추가 차감 + DepositHistory(PENALTY) |
| diff < 0 | 환급 + DepositHistory(REFUND) |
| diff = 0 | 변동 없음 |

**Response 200 data:** API 20과 동일 형식

---

### API 22: GET /api/v1/admin/attendances/sessions/{sessionId}/summary [가산점]

현재 기수 전체 CohortMember의 출결 통계

**Response 200 data (배열):**
```json
[{
  "memberId", "memberName", "present", "absent", "late", "excused",
  "totalPenalty", "deposit"
}]
```

---

### API 23: GET /api/v1/admin/attendances/members/{memberId} [가산점]

**Response 200 data:**
```json
{
  "memberId", "memberName", "generation", "partName", "teamName",
  "deposit", "excuseCount",
  "attendances": [{ "id", "sessionId", "memberId", "status", "lateMinutes", "penaltyAmount", "reason", "checkedInAt", "createdAt", "updatedAt" }]
}
```

---

### API 24: GET /api/v1/admin/attendances/sessions/{sessionId} [가산점]

**Response 200 data:**
```json
{
  "sessionId", "sessionTitle",
  "attendances": [{ "id", "sessionId", "memberId", "status", "lateMinutes", "penaltyAmount", "reason", "checkedInAt", "createdAt", "updatedAt" }]
}
```

---

### API 25: GET /api/v1/admin/cohort-members/{cohortMemberId}/deposits [가산점]

**Response 200 data (배열):**
```json
[{
  "id", "cohortMemberId", "type": "INITIAL|PENALTY|REFUND",
  "amount", "balanceAfter", "attendanceId",
  "description", "createdAt"
}]
```
- amount: INITIAL/REFUND=양수, PENALTY=음수

---

## 핵심 비즈니스 컴포넌트

### PenaltyCalculator (@Component)
```
PRESENT → 0
ABSENT → 10,000
EXCUSED → 0
LATE → min(lateMinutes × 500, 10,000)
```

### DepositService
- applyInitialDeposit(CohortMember) → deposit=100,000 + DepositHistory(INITIAL, +100000)
- deductPenalty(CohortMember, amount, Attendance) → 잔액 < amount → DEPOSIT_INSUFFICIENT. 아니면 차감 + DepositHistory(PENALTY, -amount)
- refund(CohortMember, amount, Attendance) → 환급 + DepositHistory(REFUND, +amount)

### ExcusePolicy
- toExcused(CohortMember) → excuseCount++, >3이면 EXCUSE_LIMIT_EXCEEDED
- fromExcused(CohortMember) → excuseCount-- (min 0)

---

## 단위 테스트 (서비스 레이어)

### 테스트 클래스

1. **AuthServiceTest**: 로그인 성공/실패/탈퇴회원
2. **MemberServiceTest**: 등록(중복 loginId), 조회, 수정, 탈퇴
3. **SessionServiceTest**: 생성(+QR 자동), 수정(CANCELLED 불가), 삭제
4. **QrCodeServiceTest**: 생성(활성 QR 있으면 거부), 갱신(만료+새생성)
5. **AttendanceServiceTest** (가산점):
   - QR 출석 PRESENT (startTime 이전)
   - QR 출석 LATE 7분 → 3,500원, 보증금 차감
   - 검증 순서: QR_INVALID 에러코드 정확성
   - DEPOSIT_INSUFFICIENT: 잔액 부족
   - 출결 수정: LATE(3000)→ABSENT(10000) → diff 7,000 추가차감
   - 출결 수정: ABSENT(10000)→PRESENT(0) → 10,000 환급
   - 공결 제한: 4회째 → EXCUSE_LIMIT_EXCEEDED
   - EXCUSED→PRESENT: excuseCount 감소

### 테스트 규칙
- `Clock.fixed(Instant, ZoneId.of("Asia/Seoul"))` 주입
- Repository는 `@Mock`
- Service에 `@InjectMocks`
- `@DisplayName`으로 한글 설명

---

## application.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:prography;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create
    show-sql: true
    properties:
      hibernate:
        format_sql: true

app:
  current-cohort-number: 11
```

---

## ClockConfig

```java
@Configuration
public class ClockConfig {
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
```

---

## 산출물 디렉토리 구조

```
prography-11th-backend/
├── docs/
│   ├── ERD.md                    # Mermaid erDiagram
│   ├── SYSTEM_ARCHITECTURE.md    # 현재 + 이상적 아키텍처 (Mermaid)
│   ├── DECISIONS.md              # 설계 결정 사항
│   └── AI_USAGE.md               # AI 사용 내역
├── README.md                     # 실행 방법 필수 포함
├── build.gradle.kts
└── src/
```

### README.md 필수 내용:
```
# 실행 방법
## 요구사항: JDK 17+

## 빌드 및 실행
./gradlew bootRun

## 테스트
./gradlew test

## API 문서
http://localhost:8080/swagger-ui.html

## H2 콘솔
http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:prography)
```

### docs/ERD.md
위 엔티티 설계를 Mermaid erDiagram 문법으로 작성

### docs/SYSTEM_ARCHITECTURE.md
**현재**: Client → Controller → Service → Repository → H2
**이상적**: API Gateway → Auth/Attendance/Member Service (독립 DB) + Redis QR 캐싱 + Kafka 이벤트 (AttendanceCreated → Deposit Service)

### docs/DECISIONS.md
- CohortMember 분리 이유
- 검증 순서 고정 이유
- PenaltyCalculator/DepositService 분리 이유
- Clock 주입으로 시간 테스트 안정화
- Soft-delete 정책

---

## 구현 순서

1. 프로젝트 스캐폴딩 (build.gradle.kts, application.yml, 패키지)
2. 공통 (ApiResponse, ErrorCode, BusinessException, GlobalExceptionHandler)
3. 엔티티 + Repository
4. 시드 데이터
5. 필수 API 16개 (Auth → Member → Cohort → Session → QR)
6. 가산점 API 9개 (Attendance → Deposit)
7. 단위 테스트
8. docs/ + README.md

---

## 주의사항

- 주석 달지 마.
- import문 생략하지 마.
- 모든 파일을 완전한 형태로 작성해.
- 컴파일 에러 없이 `./gradlew bootRun`으로 즉시 실행 가능해야 함.
- `admin` / `admin1234`로 로그인 가능해야 함.
- 현재 기수(11기)는 application.yml 설정값으로 관리. 하드코딩 금지.
- 모든 날짜/시간은 Asia/Seoul 타임존 기준.
- 응답의 Instant 필드는 ISO-8601 UTC 포맷 (Z suffix).
