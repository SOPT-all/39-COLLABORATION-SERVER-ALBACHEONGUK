package org.sopt.alba.domain.recommendation.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationErrorCode implements ErrorCode {

    INVALID_SIDO(HttpStatus.BAD_REQUEST, "시·도 ID가 아닙니다.");

    private final HttpStatus status;
    private final String message;
}