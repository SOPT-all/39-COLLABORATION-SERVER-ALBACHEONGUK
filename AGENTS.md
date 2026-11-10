# 개발 규칙

이 규칙은 저장소 전체에 적용합니다. 브랜치, 커밋, PR, 리뷰 절차는 [README의 협업 규칙](README.md#협업-규칙)을 따릅니다. `main`에 직접 푸시하지 않습니다.

## 패키지와 의존성

- 기본 패키지는 `org.sopt.alba`입니다. 여러 도메인에서 재사용하는 응답, 예외, 설정, 기반 엔티티는 `common`에 둡니다.
- 기능 코드는 `domain/job`, `domain/recommendation`, `domain/search`, `domain/scrap`, `domain/member`에 둡니다. 도메인 안에서 필요한 `controller`, `service`, `repository`, `entity`, `dto`, `code` 패키지를 사용합니다.
- 호출은 Controller → Service → Repository 방향으로 흐릅니다. 다른 도메인의 데이터가 필요하면 해당 도메인의 Service를 호출합니다.
- 도메인 간 의존은 `recommendation → job`, `scrap → job`처럼 한 방향으로 유지하고 순환 의존을 만들지 않습니다.
- 엔티티는 응답 DTO로 변환해서 반환합니다.

## 네이밍

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 패키지 | 소문자, 언더스코어 없이 | `domain.job.service` |
| 클래스·Enum·Record | PascalCase | `JobPosting`, `SortType` |
| 메서드 | camelCase, 동사로 시작 | `findJobs`, `scrapJob` |
| 변수·필드 | camelCase, 명사 | `jobs`, `closingDate` |
| 상수 | UPPER_SNAKE_CASE | `MAX_RECENT_KEYWORD_COUNT` |
| boolean | `is` 또는 `has` 접두어 | `isClosed`, `hasWorkTime` |
| DB 테이블·컬럼 | snake_case, 테이블은 단수형 | `job_posting`, `closing_date` |
| API 경로 | kebab-case, 복수 명사 | `/api/v1/job-postings` |
| 쿼리 파라미터 | camelCase | `ongoingOnly` |

- 단건 조회는 `get`으로 시작하고, 대상이 없으면 예외를 던집니다. 조건 조회에는 `find`를 쓰고, 생성·수정·삭제에는 `create`, `update`, `delete`를 씁니다.
- 계층 클래스는 `{도메인}Controller`, `{도메인}Service`, `{도메인}Repository`로 이름을 짓습니다.
- DTO는 `JobSearchRequest`처럼 `{도메인}{동작}Request` 또는 `{도메인}{동작}Response`로 이름을 짓고, 가능하면 record를 사용합니다.
- 도메인 응답 코드는 `{도메인}ErrorCode`와 `{도메인}SuccessCode`로 이름을 짓습니다. 약어는 `cnt` 대신 `count`처럼 풀어 씁니다.

## API, 검증, 예외

- 비즈니스 API는 `ResponseEntity<CommonResponse<T>>`를 반환합니다. 성공 응답은 `CommonResponse.success(...)`로 만들며 JSON 필드는 `status`, `code`, `message`, `data`입니다. 기존 헬스체크는 문자열 `OK`를 반환합니다.
- 성공과 실패 코드는 각각 `SuccessCode`, `ErrorCode`를 구현하는 enum으로 정의합니다. 공통 코드는 `common/code`, 도메인 코드는 `domain/{도메인}/code`에 둡니다. 조회는 200, 생성은 201을 사용하며 HTTP 상태는 코드 enum에서 관리합니다.
- 비즈니스 예외는 `BusinessException(ErrorCode)`로 던지고 `GlobalExceptionHandler`에서 처리합니다. 예상하지 못한 예외의 상세 원인은 로그에 남기고 클라이언트에는 일반 메시지로 500을 응답합니다.
- 잘못된 입력, 필수 파라미터 누락, 검증 실패는 400을, 지원하지 않는 HTTP 메서드는 405를 반환합니다. 요청 검증은 `@Valid`와 Bean Validation을 사용하고 검증 메시지는 한국어로 지정합니다.
- 페이징 목록에 `page`, `size`, `totalElements`, `totalPages`를 포함합니다. 다중 필터는 `?region=서울&region=경기`처럼 동일한 쿼리 파라미터를 반복합니다.
- 목업 회원 식별이 필요한 API는 기존 `@CurrentMember`와 `X-Member-Id`를 사용합니다.

## 설정과 목데이터

- 공통 설정은 `application.yml`, 환경별 설정은 `application-local.yml`, `application-dev.yml`에 둡니다. Jackson 시간대는 `Asia/Seoul`을 사용합니다.
- 비밀번호나 API 키는 환경변수로 주입합니다. 설정 파일 예시를 공유할 때는 실제 값 없이 `.example` 파일로 제공합니다.
- 기존 `ApplicationRunner` 초기화 코드는 `common/init`에 있습니다. 목데이터 초기화는 `local`, `dev` 프로필에서만 실행하고, 기존 데이터가 있으면 건너뜁니다. 목데이터 원본과 최종 입력 방식은 팀에서 정합니다.
- 공고 마감일은 고정 날짜 대신 `LocalDate.now(clock)` 기준 상대 날짜로 만듭니다. 마감 임박(D-3 이하), 마감 지남, 상시모집, 급여 환산 불가 사례를 포함합니다.
