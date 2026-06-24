# Internal Employee Portal — 사내 직원 관리 시스템

비트컴퓨터 사내 직원 관리 시스템 백엔드 API 서버.  
Spring Boot REST API + Vue.js SPA 완전 분리 구조.

---

## 공개 URL

| 항목 | 주소 |
|---|---|
| **API 서버** | http://43.201.77.109 |
| **Swagger UI** | http://43.201.77.109/swagger-ui/index.html |

> Swagger UI에서 우측 상단 **Authorize** 버튼 → `Bearer {JWT 토큰}` 입력 후 전체 API 테스트 가능

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
| 배포 | AWS EC2 (Ubuntu 26.04) + Nginx + Docker |
| CI/CD | GitHub Actions |

---

## 주요 기능

### 인증 (JWT)
- 이메일/비밀번호 로그인 → Access Token 발급
- 모든 인증 실패는 HTTP 401 단일 코드로 통일 (원인 미노출)
- Access Token TTL: 30분

### 퇴사자 즉시 차단 — tokenVersion 이중 잠금
퇴사 처리 API 호출 즉시 해당 직원의 모든 기존 토큰을 무효화합니다.

```
퇴사 처리 → Employee.resign()
  ├── status = RESIGNED         → isEnabled() = false → 즉시 401
  └── tokenVersion++            → JWT claim과 불일치 → 즉시 401
```

### 외부 API 장애 격리 — CircuitBreaker
```
BackgroundCheckClient (WebClient)
  → Resilience4j CircuitBreaker
      - 슬라이딩 윈도우 5회 중 50% 실패 시 OPEN (30초 차단)
      - 5xx / Timeout만 실패로 집계 (4xx · BusinessException은 무시)
  → OPEN 상태 시 즉시 503 반환
```

---

## API 엔드포인트

```
POST   /api/auth/login                              로그인 (JWT 발급)

GET    /api/me                                      내 정보 조회        ROLE_USER
PATCH  /api/me                                      내 정보 수정        ROLE_USER

POST   /api/admin/employees                         직원 계정 생성      ROLE_ADMIN
GET    /api/admin/employees                         전체 직원 목록      ROLE_ADMIN
GET    /api/admin/employees/{id}                    직원 상세 조회      ROLE_ADMIN
PATCH  /api/admin/employees/{id}/status             퇴사 처리           ROLE_ADMIN

POST   /api/admin/background-checks                 배경 조회 요청      ROLE_ADMIN
GET    /api/admin/background-checks/{checkId}       배경 조회 결과      ROLE_ADMIN
GET    /api/admin/background-checks?employeeId=...  직원별 조회 이력    ROLE_ADMIN
```

---

## 응답 규격

### 성공
```json
{
  "success": true,
  "data": { },
  "message": "요청이 성공적으로 처리되었습니다.",
  "timestamp": "2026-06-24T12:00:00"
}
```

### 실패
```json
{
  "success": false,
  "code": "E001",
  "message": "직원을 찾을 수 없습니다.",
  "errors": [],
  "timestamp": "2026-06-24T12:00:00"
}
```

### 에러 코드

| 코드 | HTTP | 설명 |
|---|---|---|
| C001 | 400 | 입력값 오류 |
| A001 | 401 | 인증 실패 (원인 미노출) |
| A002 | 403 | 권한 없음 |
| E001 | 404 | 직원 없음 |
| E002 | 409 | 이메일 중복 |
| X001 | 503 | 외부 서비스 불가 (CircuitBreaker OPEN) |
| X002 | 504 | 외부 서비스 타임아웃 |
| X003 | 502 | 외부 API 오류 |
| X004 | 404 | 배경 조회 결과 없음 |

---

## 패키지 구조

```
src/main/java/com/bit/portal/
├── global/
│   ├── config/          # Security, CORS, Swagger, WebClient, Resilience4j
│   ├── error/           # ErrorCode, BusinessException, GlobalExceptionHandler
│   ├── response/        # ApiResponse<T>
│   └── security/        # JwtTokenProvider, JwtAuthenticationFilter
└── domain/
    ├── auth/            # POST /api/auth/login
    ├── employee/        # 직원 CRUD, 퇴사 처리
    └── background/      # 배경 조회 외부 API 연동
```

---

## 로컬 실행 (Docker Compose)

```bash
git clone https://github.com/heainKang/internal-employee-portal.git
cd internal-employee-portal

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
        ├── gradle bootJar -x test
        ├── SCP → EC2 /opt/portal/app.jar
        └── systemctl restart portal

EC2 (Ubuntu 26.04, t3.micro)
  ├── Nginx :80 → proxy_pass localhost:8080
  ├── portal.service (systemd) → java -jar app.jar
  └── Docker: postgres:16 컨테이너
```

### 환경 변수 (EC2 `/opt/portal/.env`)

| 변수 | 설명 |
|---|---|
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` | DB 사용자 |
| `DB_PASSWORD` | DB 비밀번호 |
| `JWT_SECRET_KEY` | JWT 서명 키 (32자 이상) |
| `JWT_EXPIRATION` | 토큰 유효시간(초), 기본 1800 |
| `DDL_AUTO` | Hibernate DDL 전략 |
| `ADMIN_INITIAL_EMAIL` | 초기 관리자 이메일 |
| `ADMIN_INITIAL_PASSWORD` | 초기 관리자 비밀번호 |
| `CORS_ALLOWED_ORIGINS` | 허용할 프론트엔드 Origin |

`.env.example` 파일 참고.
