package org.sopt.alba.domain.recommendation.enums;

public enum RecommendationStatus {
    OK,                  // 추천 공고 1~3건
    NO_RESUME,           // 이력서 없음 > 이력서 작성하기
    DRAFT_ONLY,          // 작성 중인 이력서만 있음 > 이력서 이어 작성하기
    NO_RECOMMENDATION,   // 조건에 맞는 추천 공고 없음 > 이력서 수정하기
    GUEST                // 비로그인 > 로그인하고 추천 받기 띄우기
}