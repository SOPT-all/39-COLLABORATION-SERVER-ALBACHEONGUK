package org.sopt.alba.domain.member.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberSuccessCode implements SuccessCode {

    GET_MEMBER_PROFILE_SUCCESS(HttpStatus.OK, "내 프로필 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}
