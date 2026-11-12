package org.sopt.alba.domain.job.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JobImageSuccessCode implements SuccessCode {
    GET_JOB_IMAGE_SUCCESS(HttpStatus.OK, "공고 이미지 주소를 발급했습니다.");

    private final HttpStatus status;
    private final String message;
}
