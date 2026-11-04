package org.sopt.alba.common.code;

import org.springframework.http.HttpStatus;

public interface SuccessCode {

    String name();

    HttpStatus getStatus();

    String getMessage();
}