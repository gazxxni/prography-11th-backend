# System Architecture

## Current Architecture

```mermaid
flowchart TD
    Client[Web or Admin Client]
    Controller[REST Controllers\n/api/v1/*]
    Service[Domain Services\nAuth Member Cohort Session Attendance Deposit]
    Policy[Policy Components\nPenaltyCalculator ExcusePolicy]
    Repository[JPA Repositories]
    DB[(H2 In-Memory DB)]
    Seed[DataInitializer]
    Config[Global Config\nSecurity Clock Swagger Exception Handler]

    Client --> Controller
    Controller --> Service
    Service --> Policy
    Service --> Repository
    Repository --> DB
    Seed --> Repository
    Config --> Controller
    Config --> Service
```

## Ideal Next Architecture

```mermaid
flowchart TD
    Client[Client]
    Gateway[API Gateway or BFF]
    Auth[Auth Service]
    Attendance[Attendance Service]
    Member[Member Service]
    Ledger[Deposit Ledger Service]
    EventBus[Event Bus]
    ReadModel[Reporting Read Model]
    RDB[(PostgreSQL)]
    Cache[(Redis)]
    Observability[Logging Metrics Tracing]

    Client --> Gateway
    Gateway --> Auth
    Gateway --> Attendance
    Gateway --> Member
    Attendance --> Ledger
    Attendance --> EventBus
    Member --> RDB
    Attendance --> RDB
    Ledger --> RDB
    EventBus --> ReadModel
    Attendance --> Cache
    Auth --> Cache
    Gateway --> Observability
    Auth --> Observability
    Attendance --> Observability
    Member --> Observability
    Ledger --> Observability
```

## Notes

- H2 인메모리 DB 사용 — 과제용이라 외부 DB 연결 없음
- 현재 기수는 `app.current-cohort-number` 설정으로 관리
- 패널티 차감과 보증금 변동은 서비스 레이어에서 처리
- 실서비스라면 PostgreSQL, Redis, 이벤트 버스 구조로 전환 필요
