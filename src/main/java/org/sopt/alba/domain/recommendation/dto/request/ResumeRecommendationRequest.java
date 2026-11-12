package org.sopt.alba.domain.recommendation.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

import org.sopt.alba.domain.job.enums.Workday;

/*
 * GET /api/v1/recommendations/resume 쿼리 파라미터
 * -메인 화면에서는 resumeId만 보내거나 아무것도 안 보낸다.
 * -검색 결과 화면에서는 지금 검색 조건(필터링?)을 같이 보낸다.
 */

// TODO 근무기간이랑 급여 조건 필터는 공고 검색 API의 파라미터가 확정되면 같은 이름으로 추가하기

public record ResumeRecommendationRequest(
        Long memberId,
        Long resumeId,
        String keyword,
        List<Long> regionIds,
        List<String> categoryCodes,
        List<String> workdays) {

    public ResumeRecommendationRequest {
        regionIds = withoutEmpty(regionIds);
        categoryCodes = withoutEmpty(categoryCodes).stream().map(String::trim).filter(code -> !code.isEmpty()).toList();
        workdays = withoutEmpty(workdays);
    }

    private static <T> List<T> withoutEmpty(List<T> values) {
        if (values == null) { return List.of(); }
        return values.stream().filter(Objects::nonNull).toList();
    }
}