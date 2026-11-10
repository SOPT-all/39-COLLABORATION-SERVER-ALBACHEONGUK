# 알바천국 PC 웹 공고 탐색 서버

SOPT 39기 합동세미나 웹 2팀의 알바천국 리디자인 프로젝트입니다. 공고 검색과 추천 화면에 필요한 API를 제공하며, 시연에는 목데이터를 사용합니다.

## 기술 스택

- Java 21, Spring Boot 3.5.0, Gradle Wrapper
- Spring Web, Spring Data JPA, Bean Validation, Lombok
- QueryDSL로 공고 필터와 정렬 조건을 조합합니다.
- 로컬 DB는 H2를 사용합니다. API 문서는 Springdoc Swagger UI로 확인합니다.

공고, 추천, 검색, 스크랩, 회원은 도메인별로 나눕니다. 기능 관련 코드를 한곳에 두면 담당을 나누기 쉽고 수정할 범위를 찾기 편합니다. 코드와 설정을 작성할 때는 [개발 규칙](AGENTS.md)을 따릅니다.

## 로컬 실행

JDK 21을 설치한 뒤 저장소 루트에서 실행합니다.

```sh
./gradlew bootRun --args='--spring.profiles.active=local'
```

Windows에서는 `gradlew.bat bootRun --args="--spring.profiles.active=local"`을 사용합니다.

- 헬스체크는 `GET /api/v1/health`입니다.
- Swagger UI는 `/swagger-ui/index.html`입니다.
- `bootJar`로 빌드한 실행 파일은 `build/libs/app.jar`에 생성됩니다.

## 협업 규칙

### 작업 흐름

1. [이슈 템플릿](.github/ISSUE_TEMPLATE/issue-template.md)으로 이슈를 만듭니다.
2. `main`에서 `feat/#이슈번호/영문-작업명` 브랜치를 만듭니다. 예시는 `feat/#8/team-conventions`입니다.
3. 작업 브랜치를 푸시하고 [PR 템플릿](.github/PULL_REQUEST_TEMPLATE.md)으로 `main` 대상 PR을 만듭니다. 관련 이슈를 `close #번호`로 연결하고, 해당하면 Postman 확인 결과를 첨부합니다.
4. 팀원 모두의 승인을 받은 뒤 Merge Commit으로 머지합니다. 머지한 작업 브랜치는 필요에 따라 삭제합니다.

`main`에 직접 푸시하는 것은 금지합니다. 리뷰에서는 본인 코드처럼 꼼꼼히 확인합니다.

### 커밋 메시지

`type: 내용` 형식을 사용합니다. 타입은 영어로, 콜론 뒤의 내용은 한국어로 씁니다. 예시는 `feat: 공고 검색 API 구현`과 `docs: 팀 개발 컨벤션 정리`입니다.

| 타입 | 용도 |
| --- | --- |
| `feat` | 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 동작을 유지하는 코드 구조 개선 |
| `chore` | 빌드·설정 등 유지보수 |
| `docs` | 문서 변경 |
| `test` | 테스트 추가·수정 |
| `style` | 동작을 유지하는 포맷 변경 |

### 소통

- 연락을 확인하면 바로 처리할 수 있는 일은 미루지 않습니다. 바로 처리하기 어렵다면 이유를 짧게 공유하고, 서로 부드럽게 말합니다.
- 회의는 디스코드에서 필요한 안건 위주로 짧게 진행합니다.
- 일정이 밀리거나 작업이 막히면 바로 공유하고 같이 해결합니다. 다른 파트에 영향이 있다면 합동세미나 단톡방에 바로 알립니다.
- AI를 활용해 구현한 코드도 왜 그렇게 작성했는지 설명할 수 있어야 합니다.
