package org.sopt.alba.common.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StorageErrorCode implements ErrorCode {
    STORAGE_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "이미지 저장소가 설정되지 않았습니다."),
    INVALID_IMAGE_PATH(HttpStatus.BAD_REQUEST, "올바르지 않은 이미지 경로입니다."),
    STORAGE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "이미지 주소를 발급할 수 없습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String message;
}
