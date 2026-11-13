package org.sopt.alba.domain.search.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SearchErrorCode implements ErrorCode {

    INVALID_POPULAR_KEYWORD_SIZE(HttpStatus.BAD_REQUEST, "size는 5 또는 10이어야 합니다.");

    private final HttpStatus status;
    private final String message;
}
