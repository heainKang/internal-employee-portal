# Internal Employee Portal — CLAUDE.md
> Claude Code 세션 자동 인지용 컨텍스트 파일

---

## 프로젝트 개요

비트컴퓨터 사내 직원 관리 시스템(Internal Employee Portal) 백엔드 API 서버.
완전 분리 구조: **Spring Boot REST API** ↔ **Vue.js SPA** (별도 레포)

---

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security 6 + JWT (jjwt) |
| ORM | Spring Data JPA + Hibernate |
| Database | MSSQL (Microsoft SQL Server) |
| Build Tool | Gradle (Kotlin DSL) |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| HTTP Client | Spring WebFlux WebClient |
| 장애 격리 | Resilience4j CircuitBreaker |
| 외부 API | Background Check API (AWS API Gateway) |

---

## 패키지 구조

```
src/main/java/com/bit/portal/
├── global/                          # 공통(횡단 관심사)
│   ├── config/
│   │   ├── SecurityConfig.java      # Spring Security + JWT 필터 등록
│   │   ├── CorsConfig.java          # CORS (Vue.js 클라이언트 허용)
│   │   ├── SwaggerConfig.java       # SpringDoc OpenAPI 설정
│   │   ├── WebClientConfig.java     # 외부 API 클라이언트 (타임아웃 설정)
│   │   └── ResilienceConfig.java    # CircuitBreaker 설정
│   ├── error/
│   │   ├── code/
│   │   │   └── ErrorCode.java       # 에러 코드 enum (전체 중앙 관리)
│   │   ├── exception/
│   │   │   ├── BusinessException.java       # 비즈니스 예외 베이스
│   │   │   └── ExternalApiException.java    # 외부 API 예외
│   │   ├── handler/
│   │   │   └── GlobalExceptionHandler.java  # @RestControllerAdvice
│   │   └── response/
│   │       └── ErrorResponse.java   # 에러 응답 JSON 구조
│   ├── response/
│   │   └── ApiResponse.java         # 표준 성공 응답 래퍼 (제네릭)
│   └── security/
│       ├── JwtTokenProvider.java            # 토큰 생성/검증
│       ├── JwtAuthenticationFilter.java     # 요청마다 JWT 검증 필터
│       └── CustomUserDetailsService.java    # DB 조회 + 계정 상태 체크
│
└── domain/                          # 비즈니스 도메인
    ├── auth/
    │   ├── controller/AuthController.java
    │   ├── dto/
    │   │   ├── LoginRequest.java
    │   │   └── LoginResponse.java
    │   └── service/AuthService.java
    ├── employee/
    │   ├── controller/
    │   │   ├── EmployeeController.java      # /api/me (직원 본인)
    │   │   └── AdminController.java         # /api/admin/** (관리자)
    │   ├── dto/
    │   │   ├── EmployeeCreateRequest.java
    │   │   ├── EmployeeUpdateRequest.java
    │   │   └── EmployeeResponse.java
    │   ├── entity/
    │   │   └── Employee.java
    │   ├── enums/
    │   │   ├── EmployeeStatus.java          # ACTIVE, RESIGNED
    │   │   └── Role.java                    # ROLE_USER, ROLE_ADMIN
    │   ├── repository/EmployeeRepository.java
    │   └── service/EmployeeService.java     # 비즈니스 로직 전담
    └── background/
        ├── controller/BackgroundCheckController.java  # /api/admin/background-checks
        ├── client/BackgroundCheckClient.java          # WebClient 외부 API 호출
        ├── dto/
        │   ├── BackgroundCheckCreateRequest.java
        │   ├── BackgroundCheckCreatedResponse.java
        │   ├── BackgroundCheckResultResponse.java
        │   └── BackgroundCheckListResponse.java
        └── service/BackgroundCheckService.java
```

---

## API 엔드포인트

```
POST   /api/auth/login                              # 로그인 (JWT 발급)

GET    /api/me                                      # 내 정보 조회 (ROLE_USER)
PATCH  /api/me                                      # 내 정보 수정 (ROLE_USER)

POST   /api/admin/employees                         # 직원 계정 생성 (ROLE_ADMIN)
GET    /api/admin/employees                         # 전체 직원 목록 (ROLE_ADMIN)
GET    /api/admin/employees/{id}                    # 직원 상세 조회 (ROLE_ADMIN)
PATCH  /api/admin/employees/{id}/status             # 직원 상태 변경 / 퇴사 처리 (ROLE_ADMIN)

POST   /api/admin/background-checks                 # 배경 조회 요청 (ROLE_ADMIN)
GET    /api/admin/background-checks/{checkId}       # 배경 조회 결과 (ROLE_ADMIN)
GET    /api/admin/background-checks?employeeId=...  # 직원별 조회 이력 (ROLE_ADMIN)
```

---

## 표준 응답 규격

### 성공 응답 — message는 상황에 따라 유연하게 설정
```json
{ "success": true, "data": { }, "message": "요청이 성공적으로 처리되었습니다.", "timestamp": "..." }
{ "success": true, "data": null, "message": "직원 계정이 생성되었습니다.", "timestamp": "..." }
```

**ApiResponse 사용법:**
- `ApiResponse.of(data)` → 기본 메시지 "요청이 성공적으로 처리되었습니다."
- `ApiResponse.of(data, "커스텀 메시지")` → 커스텀 메시지
- `ApiResponse.ok()` → data null, 기본 메시지
- `ApiResponse.ok("커스텀 메시지")` → data null, 커스텀 메시지

### 에러 응답
```json
{
  "success": false,
  "code": "E001",
  "message": "직원을 찾을 수 없습니다",
  "errors": [
    { "field": "email", "message": "이메일 형식이 올바르지 않습니다" }
  ],
  "timestamp": "2025-01-15T09:30:00Z"
}
```

---

## 에러 코드 체계

| 범위 | 코드 | 설명 |
|---|---|---|
| 공통 | C001~C005 | 입력값 오류, 인증, 권한, 404, 서버 오류 |
| 인증/보안 | A001~A004 | 토큰 오류, 만료, 비밀번호 불일치, 계정 비활성화 |
| 직원 | E001~E003 | 직원 없음, 이메일 중복, 퇴사자 접근 |
| 외부 API | X001~X004 | 서비스 불가, 타임아웃, API 오류, 결과 없음 |

---

## 퇴사자 즉시 차단 전략

**채택 방식: isEnabled() + tokenVersion 이중 잠금**

1. `Employee.isEnabled()` → status == ACTIVE 여부 반환
2. `JwtAuthenticationFilter`에서 `loadUserByUsername()` 호출 후 `isEnabled()` 명시적 체크
3. 비활성 계정 → 즉시 401 반환 (상세 사유 노출 금지)
4. JWT claim에 `tokenVersion` 포함 → 퇴사 처리 시 DB tokenVersion 증가
5. Filter에서 claim의 tokenVersion ≠ DB tokenVersion이면 즉시 401 차단

→ 퇴사 처리 API 호출 즉시, 해당 직원의 모든 기존 토큰 무효화

---

## 외부 API 장애 격리 전략

```
[BackgroundCheckClient]
  → WebClient (connectionTimeout: 3s, readTimeout: 10s)
  → Resilience4j CircuitBreaker
      - failureRateThreshold: 50%
      - waitDurationInOpenState: 30s
      - slidingWindowSize: 5 (최근 5회 중 절반 이상 실패 시 OPEN)
  → OPEN 상태 시 fallback: ExternalApiException(EXTERNAL_API_UNAVAILABLE)
  → GlobalExceptionHandler → 503 응답 반환
```

---

## JWT 보안 및 관리 규칙

> **이 규칙은 Agent 자동 인지 대상입니다. 위반 시 보안 취약점 발생.**

### 절대 금지 사항
- `JWT_SECRET_KEY` 값을 코드(.java), 설정 파일(.yml, .properties), 문서(.md) 어디에도 **하드코딩 금지**
- 토큰 만료 시간(`jwt.expiration`) 값을 에러 메시지나 응답 바디에 노출 금지
- 인증 실패 원인(잘못된 비밀번호 / 계정 없음 / 토큰 만료 / 계정 비활성화)을 구분하여 응답 금지

### 필수 준수 사항
- Secret Key는 반드시 환경 변수 `${JWT_SECRET_KEY}`로만 주입
- 토큰 만료(`ExpiredJwtException`), 서명 불일치(`SignatureException`), 계정 비활성화 등 **모든 인증 실패는 HTTP 401 단일 코드로 통일**
- 에러 메시지: "인증에 실패했습니다." 고정 (원인 불노출)
- Access Token TTL: 30분 (`${JWT_EXPIRATION:1800}`) — 코드에 숫자 직접 입력 금지
- JWT Payload에 포함 가능: sub(email), employeeId, role, tokenVersion, iat, exp
- JWT Payload에 포함 금지: password, 주민번호 등 민감 개인정보

### application.yml 패턴
```yaml
jwt:
  secret: ${JWT_SECRET_KEY}          # 환경변수만 허용, 기본값 설정 금지
  expiration: ${JWT_EXPIRATION:1800}  # 기본값 1800초(30분)
```

---

## 코딩 규칙

- **비즈니스 로직은 Service 레이어에만 작성** (Controller는 요청/응답 위임만)
- Controller: `@Validated` + DTO 바인딩만 처리
- Service: 트랜잭션, 비즈니스 로직, 외부 서비스 호출 조율
- Repository: JPA 쿼리만 (커스텀 쿼리는 `@Query` JPQL 우선)
- Entity: `@Setter` 금지, 상태 변경은 도메인 메서드로
- DTO: record 또는 불변 클래스 사용
- 예외: 항상 `BusinessException(ErrorCode.XXX)` 형태로 throw
- 외부 API 예외: `ExternalApiException(ErrorCode.XXX)` 형태로 throw

---

## Git 커밋 규칙

```
feat: 새로운 기능 추가
fix: 버그 수정
refactor: 기능 변경 없는 코드 개선
docs: 문서 수정 (README, CLAUDE.md 등)
test: 테스트 코드
chore: 빌드 설정, 의존성 변경
security: 보안 관련 수정
```

예시:
```
feat: 직원 배경 조회 API 연동 및 CircuitBreaker 적용
fix: 퇴사자 tokenVersion 불일치 시 필터 차단 로직 수정
security: JWT tokenVersion 이중 잠금 퇴사자 차단 구현
```

---

## 외부 API 정보

- **Background Check API**
- Base URL: `https://54capvm12g.execute-api.ap-northeast-2.amazonaws.com`
- POST `/background-checks` → 배경 조회 요청 (checkId 반환)
- GET `/background-checks/{checkId}` → 단건 결과 조회
- GET `/background-checks?employeeId={id}` → 직원 전체 이력 조회
- status: `pending` / `clear` / `flagged`
- 서버 다운 시 503, 타임아웃 시 504 응답

---

## 배포 환경

- **서버:** AWS EC2 + Nginx (리버스 프록시)
- **DB:** MSSQL
- **포트:** Spring Boot 8080 → Nginx 80/443 프록시
- **프론트:** Vue.js (별도 서버 또는 Nginx static 서빙)
