package org.sopt.alba.domain.search.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SearchSuccessCode implements SuccessCode {

    GET_POPULAR_KEYWORDS_SUCCESS(HttpStatus.OK, "인기 검색어 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}
