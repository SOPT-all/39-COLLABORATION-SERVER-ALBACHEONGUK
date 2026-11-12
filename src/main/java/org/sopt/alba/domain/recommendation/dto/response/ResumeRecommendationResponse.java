package org.sopt.alba.domain.recommendation.dto.response;

import java.util.List;
import org.sopt.alba.domain.job.dto.response.JobSummaryResponse;
import org.sopt.alba.domain.member.dto.response.ResumeSummaryResponse;
import org.sopt.alba.domain.recommendation.enums.RecommendationStatus;

public record ResumeRecommendationResponse(
        RecommendationStatus status,
        ResumeSummaryResponse baseResume,        // 추천 기준 이력서 (GUEST / NO_RESUME / DRAFT_ONLY 이면 null)
        List<ResumeSummaryResponse> resumes,   // 드롭다운 목록 - 최신 수정순, 작성 중 포함
        List<JobSummaryResponse> jobs              // 기본점수 + 가점 높은 순, 최대 3개
) {

    /* 비로그인 */
    public static ResumeRecommendationResponse guest() {
        return new ResumeRecommendationResponse(RecommendationStatus.GUEST, null, List.of(), List.of());
    }

    /* NO_RESUME, DRAFT_ONLY*/
    public static ResumeRecommendationResponse withoutBase(RecommendationStatus status,
                                                           List<ResumeSummaryResponse> resumes) {
        return new ResumeRecommendationResponse(status, null, resumes, List.of());
    }

    /*OK, NO_RECOMMENDATION */
    public static ResumeRecommendationResponse of(ResumeSummaryResponse baseResume,
                                                  List<ResumeSummaryResponse> resumes,
                                                  List<JobSummaryResponse> jobs) {
        RecommendationStatus status = jobs.isEmpty() ? RecommendationStatus.NO_RECOMMENDATION : RecommendationStatus.OK;
        return new ResumeRecommendationResponse(status, baseResume, resumes, jobs);
    }
}