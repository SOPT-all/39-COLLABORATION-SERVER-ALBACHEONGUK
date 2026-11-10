package org.sopt.alba.domain.recommendation.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.sopt.alba.domain.job.enums.Workday;

/*
 * GET /api/v1/recommendations/resume 쿼리 파라미터
 * -메인 화면에서는 resumeId만 보내거나 아무것도 안 보낸다.
 * -검색 결과 화면에서는 지금 검색 조건(필터링?)을 같이 보낸다.
 */

// TODO 근무기간이랑 급여 조건 필터는 공고 검색 API의 파라미터가 확정되면 같은 이름으로 추가하기

public record ResumeRecommendationRequest(
        @Positive(message = "resumeId는 1 이상이어야 합니다.")
        Long resumeId,                 // 드롭다운에서 고른 이력서 (없으면 default)
        @Size(max = 30, message = "검색어는 30자 이하여야 합니다.")
        String keyword,
        List<Long> regionIds,          // 지역 필터
        List<String> categoryCodes,    // 업직종 필터
        List<Workday> workdays         // 근무요일 필터
) {

    public ResumeRecommendationRequest {
        keyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        regionIds = regionIds == null ? List.of() : List.copyOf(regionIds);
        categoryCodes = categoryCodes == null ? List.of() : List.copyOf(categoryCodes);
        workdays = workdays == null ? List.of() : List.copyOf(workdays);
    }

    public boolean hasKeyword() {
        return keyword != null;
    }
}