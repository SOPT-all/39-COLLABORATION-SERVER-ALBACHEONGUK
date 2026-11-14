# 알바천국 PC 웹 리디자인 · API 서버

**39기 PLAY SOPT 합동 세미나 · 데스크탑 웹 2조**

> 검색에서 비교까지, 나에게 맞는 알바를 찾는 흐름을 지원하는 백엔드 API입니다.

| 자료 | 링크 |
| --- | --- |
| Swagger | 로컬 실행 후 [`/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html) |

## 프로젝트 소개

메인에서 검색 조건을 설정하고, 검색 결과에서 공고를 비교한 뒤, 이력서 조건과 지역에 맞는 공고 추천을 확인하는 PC 웹 리디자인 프로젝트입니다.

추천은 외부 AI 모델을 호출하지 않고 이력서와 공고의 업직종·근무 조건을 비교해 점수를 계산합니다. 최근 검색과 스크랩, 테마·대상별 고정 카드는 프론트엔드의 브라우저 저장 또는 목데이터 범위입니다.

## 기능 범위

| 도메인 | 기능 | API |
| --- | --- | --- |
| Job | 검색어·지역·업직종·요일·급여·모집 상태로 공고 조회, 구간별 목록·건수·정렬·페이지 제공 | `GET /api/v1/jobs` |
| Job | 지역·업직종·급여·요일 필터 옵션 조회 | `GET /api/v1/jobs/filters` |
| Job | 공고 이미지 서명 URL 발급 | `GET /api/v1/job-postings/{jobPostingId}/image` |
| Recommendation | 선택한 이력서 기준 공고 추천, 최대 3건 | `GET /api/v1/recommendations/resume` |
| Recommendation | 시·도별 모집 중 공고 최신순 최대 4건 | `GET /api/v1/recommendations/region?sido={sidoId}` |
| Search | 고정 순위 인기 검색어 조회 (메인 5개, 검색 결과 10개) | `GET /api/v1/search/popular-keywords?size=5 or 10` |
| Member | 목업 회원 이름과 진행 중 공고 수 조회 | `GET /api/v1/members/me` |
| Common | 서버 상태 확인 | `GET /api/v1/health` |

### 이력서 매칭 기준

- 기본 점수는 업직종 40점, 근무요일 20점, 근무기간 10점, 근무형태 15점, 관련 경력 15점으로 구성합니다.
- 기본 점수 65점 미만인 공고는 추천하지 않습니다. 우대 조건은 최대 15점의 가점으로 반영합니다.
- 필수 경력을 충족하지 못한 공고, 서비스 내 지원이 불가능한 공고, 이미 지원한 공고, 마감 공고는 추천 후보에서 제외합니다.
- 점수가 같으면 등록일이 최신인 공고, 공고 ID가 작은 공고 순으로 정렬합니다.
- 검색 화면에서는 검색 업직종과 관련된 최신 완료 이력서를 기준으로 삼습니다. 이력서를 직접 선택하면 선택한 이력서로 다시 조회합니다.

### 검색 규칙

- 지역·업직종·요일은 같은 항목 안에서 여러 값을 선택할 수 있고, 선택값 중 하나라도 맞으면 통과합니다.
- 서로 다른 필터 항목은 모두 만족해야 합니다.
- 페이지 크기는 20·30·50건이며, 노출 구간은 프리미엄 → 라이트 → 일반 순서를 유지합니다.
- 공고가 마감됐는지는 모집 상태와 한국 날짜 기준 마감일을 함께 확인합니다.

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| 언어·런타임 | Java 21 |
| 애플리케이션 | Spring Boot 3.5, Spring Web, Spring Validation |
| 데이터 접근 | Spring Data JPA, QueryDSL |
| 데이터베이스 | Supabase PostgreSQL |
| API 문서 | Springdoc OpenAPI / Swagger UI |
| 보조 도구 | Lombok, Gradle Wrapper |
| 배포 | GitHub Actions, AWS EC2, systemd, Nginx |

## 프로젝트 구조

```text
src/main/java/org/sopt/alba
├── common
│   ├── auth          # 목업 회원 식별
│   ├── code          # 공통 성공·오류 코드
│   ├── config        # JPA, 시간대, Swagger, 웹 설정
│   ├── dto           # 공통 응답
│   ├── exception     # 예외와 전역 예외 처리
│   └── storage       # Supabase Storage 연동
└── domain
    ├── job           # 공고 검색, 필터, 이미지
    ├── member        # 회원, 이력서, 지원 이력
    ├── recommendation # 이력서 매칭·지역 추천
    └── search        # 인기 검색어
```

도메인 안에서 `controller → service → repository` 순서로 의존합니다. Entity를 API 응답으로 직접 반환하지 않고 DTO로 변환합니다. 공통 성공·오류 응답은 `CommonResponse`를 사용합니다.

## 시작하기

### 준비물

- Java 21
- Git
- 개발용 Supabase PostgreSQL 접속 정보

## API 사용

### 공통 응답

성공·오류 응답은 다음 형식을 사용합니다.

```json
{
  "status": 200,
  "code": "GET_JOBS_SUCCESS",
  "message": "요청에 성공했습니다.",
  "data": {}
}
```

오류도 같은 구조이며 `data`는 `null`입니다. 파라미터 검증과 비즈니스 오류는 도메인별 오류 코드로 응답합니다.

### 목업 회원 식별

JWT 로그인 대신 `X-Member-Id` 헤더로 목업 회원을 식별합니다. 회원 프로필 조회에는 헤더가 필요합니다. 이력서 추천은 헤더가 없으면 비회원 상태를 반환합니다.

```http
X-Member-Id: 1
```

### 문서 확인

서버 실행 후 Swagger UI에서 API 파라미터와 응답 예시를 확인할 수 있습니다.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 협업 규칙

### 작업 흐름

1. 이슈를 생성합니다.
2. `main`에서 작업 브랜치를 분기합니다.
3. 작업을 완료하면 `main` 대상으로 PR을 올립니다.
4. 팀원 리뷰와 전체 승인 후 Merge Commit으로 병합합니다.
5. `main`에 직접 push하지 않습니다.

브랜치 이름은 `feat/#{issue-number}/branch-name` 형식을 사용하며 작업 설명은 영어로 씁니다.

### 커밋 메시지

접두어와 구체적인 변경 내용을 한글로 작성합니다.

```text
feat: 공고 검색 필터 추가
fix: 이력서 추천 마감 공고 제외
refactor: 지역 조회 쿼리 분리
docs: README 실행 안내 보완
```

사용 가능한 접두어는 `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style`입니다.

### 코드 원칙

- AI가 작성한 코드도 작성자가 구현 이유와 동작을 설명할 수 있어야 합니다.
- 패키지·변수는 소문자 또는 camelCase, 클래스·DTO·Enum은 PascalCase, 상수는 UPPER_SNAKE_CASE를 사용합니다.
- API 경로는 복수 명사를 kebab-case로 쓰고, 쿼리 파라미터는 camelCase로 씁니다.
- 검증은 Bean Validation과 `@Valid`를 사용하고, 검증 메시지는 한글로 작성합니다.
- 문제나 일정 변경이 다른 파트에 영향을 주면 즉시 공유합니다. 회의는 비대면으로 진행하고 안건을 중심으로 짧게 마칩니다.

## 자동 검사와 배포

- GitHub Actions CI는 `main` 대상 PR에서 Java 21로 `./gradlew build`를 실행합니다.
- `main`에 push되면 배포 워크플로가 빌드 후 EC2에 아티팩트를 전달하고, systemd 서비스를 재시작한 뒤 헬스 체크와 OpenAPI 경로를 확인합니다.
- 배포 환경의 비밀 값과 AWS 설정은 GitHub Secrets/Variables로 관리합니다.
