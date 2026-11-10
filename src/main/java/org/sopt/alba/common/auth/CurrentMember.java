package org.sopt.alba.common.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 목업 로그인: 요청 헤더 X-Member-Id 값을 Long memberId로 주입.!
 * required = true  > 헤더가 없으면 401 UNAUTHORIZED
 * required = false > 헤더가 없으면 null (비로그인 = GUEST)
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentMember {

    boolean required() default true;
}