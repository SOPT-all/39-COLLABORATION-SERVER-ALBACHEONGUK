package org.sopt.alba.domain.job.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JobErrorCode implements ErrorCode {

    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 지역입니다."),
    JOB_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 업직종입니다.");

    private final HttpStatus status;
    private final String message;
}