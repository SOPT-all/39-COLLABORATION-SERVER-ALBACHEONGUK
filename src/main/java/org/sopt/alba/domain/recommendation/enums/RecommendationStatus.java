package org.sopt.alba.domain.recommendation.enums;

/*
 * 클라가 화면 상태를 분기하는 값 정리
 * OK                 : 추천 공고 1~3개
 * NO_RESUME         : 이력서 0개
 * DRAFT_ONLY        : 작성 중 이력서만 있음
 * NO_RECOMMENDATION  : 조건에 맞는 공고 없음
 * HIDDEN            : 검색 결과 맥락상 영역 숨김
 * GUEST             : 비로그인
 */

public enum RecommendationStatus {
    OK,
    NO_RESUME,
    DRAFT_ONLY,
    NO_RECOMMENDATION,
    HIDDEN,
    GUEST
}