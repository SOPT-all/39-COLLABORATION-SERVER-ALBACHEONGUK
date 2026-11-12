package org.sopt.alba.domain.job.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JobImageErrorCode implements ErrorCode {
    JOB_POSTING_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공고입니다."),
    JOB_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "공고에 등록된 이미지가 없습니다.");

    private final HttpStatus status;
    private final String message;
}
