package org.sopt.alba.common.dto;

import org.sopt.alba.common.code.ErrorCode;
import org.sopt.alba.common.code.SuccessCode;
import org.springframework.http.ResponseEntity;

public record CommonResponse<T>(
        int status,
        String code,
        String message,
        T data
) {

    public static <T> ResponseEntity<CommonResponse<T>> success(SuccessCode successCode, T data) {
        return ResponseEntity
                .status(successCode.getStatus())
                .body(new CommonResponse<>(
                        successCode.getStatus().value(),
                        successCode.name(),
                        successCode.getMessage(),
                        data));
    }

    public static ResponseEntity<CommonResponse<Void>> success(SuccessCode successCode) {
        return success(successCode, null);
    }

    public static ResponseEntity<CommonResponse<Void>> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getMessage());
    }

    public static ResponseEntity<CommonResponse<Void>> error(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new CommonResponse<>(
                        errorCode.getStatus().value(),
                        errorCode.name(),
                        message,
                        null));
    }
}