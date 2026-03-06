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

- 현재 구현은 과제 범위에 맞춰 단일 Spring Boot 애플리케이션으로 구성했다.
- 현재 기수는 `app.current-cohort-number` 설정으로 분리했다.
- 출결 패널티와 보증금 변동은 서비스 레이어에서 일관되게 처리한다.
