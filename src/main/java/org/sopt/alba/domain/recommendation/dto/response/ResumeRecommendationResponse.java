//package org.sopt.alba.domain.recommendation.dto.response;
//
//import java.util.List;
//import org.sopt.alba.domain.job.dto.response.JobSummaryResponse;
//import org.sopt.alba.domain.member.dto.response.ResumeSummaryResponse;
//import org.sopt.alba.domain.recommendation.enums.RecommendationStatus;
//
//public record ResumeRecommendationResponse(
//        RecommendationStatus status,
//        ResumeSummaryResponse baseResume,        // 추천 기준 이력서 (없으면 null)
//        List<ResumeSummaryResponse> resumes,     // 드롭다운 목록 (최신 수정순, DRAFT 포함 → 클라가 비활성 처리)
//        List<JobSummaryResponse> jobs            // 매칭 점수 높은 순, 최대 3개
//        // TODO 카드에 매칭 이유 태그(희망 지역 일치 등)를 보여주기로 하면 jobs 항목에 matchReasons 추가 >> 기획서 보고 추가
//) {
//
//    /* GUEST: 비로그인 */
//    public static ResumeRecommendationResponse guest() {
//        return new ResumeRecommendationResponse(RecommendationStatus.GUEST, null, List.of(), List.of());
//    }
//
//    /* NO_RESUME / DRAFT_ONLY: 기준 이력서가 없는 상태 */
//    public static ResumeRecommendationResponse withoutBase(RecommendationStatus status,
//                                                           List<ResumeSummaryResponse> resumes) {
//        return new ResumeRecommendationResponse(status, null, resumes, List.of());
//    }
//
//    /* OK / NO_RECOMMENDATION / HIDDEN */
//    public static ResumeRecommendationResponse of(RecommendationStatus status, ResumeSummaryResponse baseResume,
//                                                  List<ResumeSummaryResponse> resumes,
//                                                  List<JobSummaryResponse> jobs) {
//        return new ResumeRecommendationResponse(status, baseResume, resumes, jobs);
//    }
//}