package org.sopt.alba.domain.recommendation.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationErrorCode implements ErrorCode {

    INVALID_SIDO(HttpStatus.BAD_REQUEST, "시·도 ID가 아닙니다."),
    RESUME_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "작성 완료된 이력서만 추천 기준으로 선택할 수 있습니다."),
    RESUME_NOT_OWNED(HttpStatus.FORBIDDEN, "본인의 이력서만 선택할 수 있습니다.");

    private final HttpStatus status;
    private final String message;
}