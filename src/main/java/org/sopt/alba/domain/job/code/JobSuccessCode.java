package org.sopt.alba.domain.job.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JobSuccessCode implements SuccessCode {

    GET_JOBS_SUCCESS(HttpStatus.OK, "공고 목록 조회에 성공했습니다."),
    GET_JOB_FILTERS_SUCCESS(HttpStatus.OK, "공고 필터 목록 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}