package org.sopt.alba.domain.recommendation.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationSuccessCode implements SuccessCode {

    GET_REGION_RECOMMENDATIONS_SUCCESS(HttpStatus.OK, "지역별 추천 공고 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}