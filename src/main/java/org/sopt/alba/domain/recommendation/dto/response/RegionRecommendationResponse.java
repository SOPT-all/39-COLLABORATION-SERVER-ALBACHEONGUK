package org.sopt.alba.domain.recommendation.dto.response;

import java.util.List;
import org.sopt.alba.domain.job.dto.response.JobSummaryResponse;

public record RegionRecommendationResponse(
        Long sidoId,
        String sidoName,
        List<JobSummaryResponse> jobs
) {
}