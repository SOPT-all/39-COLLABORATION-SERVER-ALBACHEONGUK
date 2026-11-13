package org.sopt.alba.domain.recommendation.dto.request;

import jakarta.validation.constraints.Size;
import org.sopt.alba.domain.job.enums.PayType;
import org.sopt.alba.domain.job.enums.WorkPeriod;
import org.sopt.alba.domain.job.enums.Workday;

import java.util.List;
import java.util.Objects;

/*
 * GET /api/v1/recommendations/resume 쿼리 파라미터
 * -메인 화면에서는 resumeId만 보내거나 아무것도 안 보낸다.
 * -검색 결과 화면에서는 지금 검색 조건(필터링?)을 같이 보낸다.
 */
public record ResumeRecommendationRequest(
        Long resumeId,
        @Size(max = 30) String keyword,
        List<Long> regionIds,
        List<String> categoryCodes,
        List<Workday> workdays,
        WorkPeriod workPeriod,
        PayType payType
) {
    public ResumeRecommendationRequest {
        keyword = keyword == null ? null : keyword.trim();
        regionIds = regionIds == null ? List.of() : regionIds;
        categoryCodes = categoryCodes == null
                ? List.of()
                : categoryCodes.stream()
                  .filter(Objects::nonNull)
                  .map(String::trim)
                  .filter(code -> !code.isEmpty())
                  .toList();
        workdays = workdays == null ? List.of() : workdays;
    }
}