package org.sopt.alba.common.auth;

import org.sopt.alba.common.code.GlobalErrorCode;
import org.sopt.alba.common.exception.BusinessException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String MEMBER_ID_HEADER = "X-Member-Id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMember.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        CurrentMember annotation = parameter.getParameterAnnotation(CurrentMember.class);
        String header = webRequest.getHeader(MEMBER_ID_HEADER);

        if (!StringUtils.hasText(header)) {
            if (annotation != null && annotation.required()) {
                throw new BusinessException(GlobalErrorCode.UNAUTHORIZED);
            }
            return null;
        }

        try {
            long memberId = Long.parseLong(header.trim());
            if (memberId <= 0) {
                throw new BusinessException(GlobalErrorCode.INVALID_MEMBER_HEADER);
            }
            return memberId;
        } catch (NumberFormatException e) {
            throw new BusinessException(GlobalErrorCode.INVALID_MEMBER_HEADER);
        }
    }
}