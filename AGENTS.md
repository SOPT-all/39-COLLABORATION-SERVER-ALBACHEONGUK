# 서버 개발 컨벤션

이 문서의 규칙은 저장소 전체에 적용합니다. AI를 활용해 작성한 코드도 작성자가 구현 이유를 설명할 수 있어야 합니다.

## 협업과 Git

- 바로 처리하기 어려운 일이 있으면 이유를 짧게 공유합니다. 작업이 막히거나 일정이 밀릴 것 같으면 팀에 바로 알리고, 다른 파트에 영향이 있으면 합동세미나 단톡방에 공유합니다.
- [이슈 템플릿](.github/ISSUE_TEMPLATE/issue-template.md)으로 이슈를 먼저 만듭니다. 제목에는 작업 내용을 적고 유형은 본문에서 선택합니다.
- `main`에서 `feat/#{issue-number}/branch-name` 형식으로 분기합니다. 작업명은 영어로 씁니다. 예시는 `feat/#8/team-conventions`입니다.
- 커밋은 `type: 내용` 형식으로 작성합니다. 타입은 영어로, 내용은 한국어로 씁니다. 예시는 `docs: 팀 개발 컨벤션 정리`입니다.
- 커밋 타입은 `feat`(기능 추가), `fix`(버그 수정), `refactor`(동작을 유지하는 구조 개선), `chore`(빌드·설정 변경), `docs`(문서 변경), `test`(테스트 추가·수정), `style`(동작을 유지하는 포맷 변경)을 사용합니다.
- [PR 템플릿](.github/PULL_REQUEST_TEMPLATE.md)으로 `main` 대상 PR을 엽니다. 제목에는 변경 내용을 한 줄로 적고, 본문에 `close #번호`로 이슈를 연결합니다. 해당하는 경우 Postman 확인 결과를 첨부합니다.
- 팀원 모두의 승인을 받은 뒤 Merge Commit으로 머지합니다. `main` 직접 푸시는 금지하며, 머지 후 작업 브랜치 삭제는 선택입니다.

## 아키텍처와 패키지

기본 패키지는 `org.sopt.alba`입니다. 기능에 필요한 코드를 도메인별로 모아 담당과 변경 범위를 파악하기 쉽게 하고, 여러 사람이 같은 패키지를 수정하면서 생기는 충돌을 줄입니다.

- 공통 응답·예외·설정·기반 엔티티는 `common`에 둡니다.
- 기능 코드는 `domain/{도메인}` 아래에 둡니다. 각 도메인에는 필요한 `controller`, `service`, `repository`, `entity`, `dto`, `code` 패키지를 둡니다.
- `job`은 공고 목록·검색·펼치기, `recommendation`은 공고 추천, `search`는 인기·최근 검색어, `scrap`은 스크랩, `member`는 회원을 담당합니다.
- 호출 방향은 Controller → Service → Repository로 유지합니다. 다른 도메인의 데이터가 필요하면 해당 도메인의 Service를 호출합니다. 도메인 간 의존은 `recommendation → job`, `scrap → job`처럼 한 방향으로 유지하며 순환 의존을 금지합니다.
- 기본 CRUD는 Spring Data JPA로 구현합니다. 동적 필터와 정렬 조건은 QueryDSL로 구성하고, 로컬 개발 DB는 H2를 사용합니다.

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
| API 경로 | kebab-case, 복수 명사, 동사 금지 | `/api/v1/job-postings` |
| 쿼리 파라미터 | camelCase | `ongoingOnly` |

- 단건 조회는 `get`으로 시작하고 대상이 없으면 예외를 던집니다. 조건 조회는 `find`, 생성·수정·삭제는 각각 `create`, `update`, `delete`로 시작합니다.
- 계층 클래스는 `{도메인}Controller`, `{도메인}Service`, `{도메인}Repository` 형식을 사용합니다.
- DTO는 `{도메인}{동작}Request` 또는 `{도메인}{동작}Response` 형식으로 이름을 짓고, 가능하면 record를 사용합니다. 예시는 `JobSearchRequest`입니다.
- 도메인 응답 코드는 `{도메인}ErrorCode`와 `{도메인}SuccessCode`로 이름을 짓습니다. 약어는 `cnt` 대신 `count`처럼 풀어 씁니다.

## API 응답과 예외 처리

- 비즈니스 API Controller는 `ResponseEntity<CommonResponse<T>>`를 반환합니다. 응답 필드는 `status`, `code`, `message`, `data`이며, 성공 응답은 `CommonResponse.success(...)`로 만듭니다. 엔티티는 응답 DTO로 변환합니다.
- 성공과 실패 코드는 각각 `SuccessCode`, `ErrorCode`를 구현하는 enum으로 정의합니다. 공통 코드는 `common/code`, 도메인 코드는 `domain/{도메인}/code`에 둡니다. 조회는 200, 생성은 201을 사용하며 HTTP 상태는 코드 enum에서 관리합니다.
- 비즈니스 예외는 `BusinessException(ErrorCode)`로 던지고 `GlobalExceptionHandler`에서 처리합니다. 예상하지 못한 예외의 상세 원인은 로그에 남기고, 클라이언트에는 일반 메시지로 500을 응답합니다.
- 잘못된 입력, 필수 파라미터 누락, 검증 실패는 400을 반환합니다. 지원하지 않는 HTTP 메서드는 405를 반환합니다. 요청 검증은 `@Valid`와 Bean Validation을 사용하고, 검증 어노테이션의 `message`는 한국어로 작성합니다.
- 페이징 목록 응답에는 `page`, `size`, `totalElements`, `totalPages`를 포함합니다. 다중 선택 필터는 `?region=서울&region=경기`처럼 같은 쿼리 파라미터를 반복합니다.

## 설정과 초기 데이터

- 공통 설정은 `application.yml`, 환경별 설정은 `application-local.yml`과 `application-dev.yml`에 둡니다. `spring.jackson.time-zone`은 `Asia/Seoul`로 지정합니다.
- DB 비밀번호와 API 키는 커밋하지 않고 환경변수로 주입합니다. 설정 예시는 실제 비밀 값을 제거한 `.example` 파일로 공유합니다.
- 기존 초기화 코드는 `common/init`에 둡니다. 초기화는 `local`과 `dev` 프로필에서만 실행하고, 대상 데이터가 이미 있으면 건너뜁니다.
- 공고 마감일은 `LocalDate.now(clock)` 기준 상대 날짜로 만듭니다. 초기 데이터에는 마감 임박(D-3 이하), 마감 지남, 상시모집, 급여 환산 불가 사례를 포함합니다.
