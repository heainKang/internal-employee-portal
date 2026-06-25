# Internal Employee Portal — 사내 직원 관리 시스템

비트컴퓨터 사내 직원 관리 시스템.  
Spring Boot REST API + Vue.js SPA 완전 분리 구조로 구성된 인사 관리 포털입니다.

---

## 공개 URL

| 항목 | 주소 |
|---|---|
| **서비스 (Vue.js SPA)** | http://43.201.77.109 |
| **API 서버** | http://43.201.77.109/api |
| **Swagger UI** | http://43.201.77.109/swagger-ui/index.html |

> Swagger UI → 우측 상단 **Authorize** → `Bearer {JWT 토큰}` 입력 후 전체 API 테스트 가능

### 테스트 계정

| 이메일 | 비밀번호 | 권한 |
|---|---|---|
| admin@bit.com | Admin1234! | ROLE_ADMIN |

---

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Security | Spring Security 6 + JWT (jjwt 0.12.6) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL 16 |
| Build | Gradle 8.8 (Kotlin DSL) |
| API 문서 | SpringDoc OpenAPI 3 (Swagger UI) |
| HTTP 클라이언트 | Spring WebFlux WebClient |
| 장애 격리 | Resilience4j CircuitBreaker |
| 외부 API | Background Check API (AWS API Gateway) |
| 배포 | AWS EC2 (Ubuntu) + Nginx + Docker |
| CI/CD | GitHub Actions |
| 프론트엔드 | Vue 3 + Vite + Pinia + Tailwind CSS |

---

## 스택 선택 이유 및 주요 설계 결정

### Spring Security를 선택한 이유

사내 직원 관리 시스템은 **안정성과 보안이 최우선**인 환경입니다.  
Spring Security는 JWT 처리, 필터 체인, 권한(ROLE) 분기를 체계적으로 지원하고, 엔터프라이즈 환경에서 오랜 기간 검증된 생태계를 갖추고 있습니다.  
인증/인가 로직을 프레임워크 수준에서 표준화함으로써 누락이나 우회 경로를 최소화했습니다.

### JWT + tokenVersion — 퇴사자 즉시 무효화

일반 JWT는 발급 후 만료 전까지 취소할 수 없다는 구조적 한계가 있습니다.  
사내 시스템에서 퇴사 처리 후 구 토큰으로 계속 접근이 가능하다면 심각한 보안 문제입니다.

이를 해결하기 위해 **isEnabled() + tokenVersion 이중 잠금** 방식을 채택했습니다.

```
퇴사 처리 API 호출
  ├── Employee.status = RESIGNED  →  isEnabled() = false
  │     └── JwtAuthenticationFilter가 매 요청마다 DB 조회 후 체크 → 즉시 401
  └── Employee.tokenVersion++
        └── JWT claim의 tokenVersion ≠ DB tokenVersion → 즉시 401
```

퇴사 처리 순간부터 해당 직원이 보유한 **모든 기존 토큰이 즉시 무효화**됩니다.

### Resilience4j — 외부 서비스 장애로부터 우리 서버 보호

Background Check API는 외부 AWS API Gateway 서비스로, 간헐적 500/503 오류와 타임아웃이 발생합니다.  
외부 서비스가 죽었을 때 우리 서버까지 함께 멈추지 않도록 **CircuitBreaker**를 적용했습니다.

쓰기/읽기 작업을 **3개 독립 CB 인스턴스**로 분리하여, GET 실패가 POST를 막거나 POST 실패가 GET을 차단하는 상황을 방지했습니다.

```
BackgroundCheckClient (WebClient)
  ├── bgCheck-create  (슬라이딩 윈도우 5회, 50% 실패 시 OPEN → 30초 차단)
  ├── bgCheck-get     (슬라이딩 윈도우 5회, 50% 실패 시 OPEN → 20초 차단)
  └── bgCheck-list    (슬라이딩 윈도우 5회, 50% 실패 시 OPEN → 20초 차단)

  4xx (비즈니스 오류) → 실패 집계 제외 (ignore-exceptions: BusinessException)
  OPEN 상태           → 즉시 X005 / 503 반환
```

---

## API 에러 대응 전략

### 1. 타임아웃 설정

외부 API 호출 단계별로 타임아웃을 분리해 빠른 실패(fast-fail)를 보장합니다.

```
connectionTimeout : 3초   — 연결 자체가 안 될 때 빠르게 포기
readTimeout       : 10초  — 응답이 늦어질 때 최대 대기 시간
```

### 2. HTTP 상태코드별 예외 분리

비즈니스 오류(4xx)와 외부 API 장애(5xx / 연결 실패 / CB OPEN)를 명확히 분리합니다.

| 상황 | 예외 타입 | HTTP |
|---|---|---|
| 4xx (잘못된 요청) | `BusinessException` | — (CB 집계 제외) |
| 500 | `ExternalApiException(X003)` | 502 |
| 503 | `ExternalApiException(X001)` | 503 |
| 연결 실패 (`WebClientRequestException`) | `ExternalApiException(X001)` | 503 |
| 타임아웃 (`TimeoutException`) | `ExternalApiException(X002)` | 504 |
| CB OPEN (`CallNotPermittedException`) | `ExternalApiException(X005)` | 503 |

### 3. 예외 일원화 — GlobalExceptionHandler

`@RestControllerAdvice`로 모든 예외를 중앙에서 포착해 일관된 JSON 형태로 응답합니다.  
`CallNotPermittedException`도 Safety net으로 별도 처리하여 CB OPEN 상태가 예외로 누수되지 않습니다.

```java
@ExceptionHandler(BusinessException.class)         → 비즈니스 오류 응답
@ExceptionHandler(ExternalApiException.class)      → 외부 API 오류 응답
@ExceptionHandler(CallNotPermittedException.class) → X005 / 503 (safety net)
```

### 4. 프론트엔드 에러 표시 전략

에러 메시지 뒤에 원인 코드를 괄호로 병기해 직관적인 진단을 돕습니다.

```
"외부 서비스를 현재 사용할 수 없습니다. (서비스 불가)"    ← X001
"외부 서비스를 현재 사용할 수 없습니다. (타임아웃)"       ← X002
"외부 서비스를 현재 사용할 수 없습니다. (500 서버 오류)"  ← X003
"배경 조회 결과를 찾을 수 없습니다. (결과 없음)"          ← X004
"외부 서비스를 현재 사용할 수 없습니다. (CB OPEN)"        ← X005
```

### 5. 폴링 에러 처리

`pending` 상태의 배경 조회 결과를 30초마다 자동 갱신하는 폴링 과정에서 발생하는 에러는 **조용히 무시**하고 다음 주기에 재시도합니다.  
사용자가 직접 새로고침을 요청한 경우에만 Toast를 통해 오류를 알립니다.

---

## API 엔드포인트

```
POST   /api/auth/login                              로그인 (JWT 발급)

GET    /api/me                                      내 정보 조회           ROLE_USER
PATCH  /api/me                                      내 정보 수정           ROLE_USER

POST   /api/admin/employees                         직원 계정 생성         ROLE_ADMIN
GET    /api/admin/employees                         전체 직원 목록         ROLE_ADMIN
GET    /api/admin/employees/{id}                    직원 상세 조회         ROLE_ADMIN
PATCH  /api/admin/employees/{id}                    직원 정보 수정         ROLE_ADMIN
                                                    (부서·직책·권한)
PATCH  /api/admin/employees/{id}/status             퇴사 처리              ROLE_ADMIN

POST   /api/admin/background-checks                 배경 조회 요청         ROLE_ADMIN
GET    /api/admin/background-checks/{checkId}       배경 조회 단건 결과    ROLE_ADMIN
GET    /api/admin/background-checks?employeeId=...  직원별 조회 이력       ROLE_ADMIN
```

---

## 응답 규격

### 성공
```json
{
  "success": true,
  "data": { },
  "message": "요청이 성공적으로 처리되었습니다.",
  "timestamp": "2026-06-25T12:00:00"
}
```

### 실패
```json
{
  "success": false,
  "code": "E001",
  "message": "직원을 찾을 수 없습니다.",
  "errors": [],
  "timestamp": "2026-06-25T12:00:00"
}
```

### 에러 코드

| 코드 | HTTP | 설명 |
|---|---|---|
| C001 | 400 | 입력값 유효성 오류 |
| C005 | 500 | 서버 내부 오류 |
| A001 | 401 | 인증 실패 (원인 미노출) |
| A002 | 403 | 권한 없음 |
| E001 | 404 | 직원을 찾을 수 없음 |
| E002 | 409 | 이메일 중복 |
| E003 | 400 | 이미 퇴사 처리된 직원 |
| X001 | 503 | 외부 서비스 불가 (연결 실패 / 503) |
| X002 | 504 | 외부 서비스 타임아웃 |
| X003 | 502 | 외부 API 500 오류 |
| X004 | 404 | 배경 조회 결과 없음 |
| X005 | 503 | CircuitBreaker OPEN (잠시 차단 중) |

---

## 패키지 구조

```
src/main/java/com/bit/portal/
├── global/
│   ├── config/          # Security, CORS, Swagger, WebClient, Resilience4j
│   ├── error/           # ErrorCode enum, BusinessException, GlobalExceptionHandler
│   ├── response/        # ApiResponse<T>, PageResponse<T>
│   └── security/        # JwtTokenProvider, JwtAuthenticationFilter
└── domain/
    ├── auth/            # POST /api/auth/login
    ├── employee/        # 직원 CRUD, 퇴사 처리, 시드 데이터
    └── background/      # 배경 조회 외부 API 연동 (WebClient + CB)
```

---

## 로컬 실행 (Docker Compose)

```bash
git clone https://github.com/heainKang/internal-employee-portal.git
cd internal-employee-portal

# 환경 변수 설정
cp .env.example .env   # 값 수정 후 실행

# PostgreSQL + Spring Boot 앱 실행
docker compose up -d

# 기동 확인 (약 30초 소요)
docker compose logs app -f
```

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- 초기 관리자 계정: `admin@bit.com` / `Admin1234!`

---

## 배포 구조

```
GitHub (main push)
  └── GitHub Actions
        ├── gradle bootJar -x test  →  app.jar 빌드
        ├── SCP → EC2 /opt/portal/app.jar
        └── systemctl restart portal

EC2 (Ubuntu, t3.micro)
  ├── Nginx :80
  │     ├── /              →  Vue.js SPA 정적 파일 (/opt/portal/frontend)
  │     ├── /api/          →  proxy_pass localhost:8080
  │     └── /swagger-ui/   →  proxy_pass localhost:8080
  ├── portal.service (systemd)  →  java -jar app.jar
  └── Docker: postgres:16 컨테이너
```

### 환경 변수 (EC2 `/opt/portal/.env`)

| 변수 | 설명 |
|---|---|
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` | DB 사용자 |
| `DB_PASSWORD` | DB 비밀번호 |
| `JWT_SECRET_KEY` | JWT 서명 키 (32자 이상 랜덤) |
| `JWT_EXPIRATION` | 토큰 유효시간(초), 기본 1800 |
| `DDL_AUTO` | Hibernate DDL 전략 (기본 update) |
| `ADMIN_INITIAL_EMAIL` | 초기 관리자 이메일 |
| `ADMIN_INITIAL_PASSWORD` | 초기 관리자 비밀번호 |
| `CORS_ALLOWED_ORIGINS` | 허용할 프론트엔드 Origin |
| `SEED_EMPLOYEES` | true 시 테스트 직원 30명 생성 (1회성) |
| `SEED_PASSWORD` | 테스트 직원 초기 비밀번호 (기본 Test1234!) |
