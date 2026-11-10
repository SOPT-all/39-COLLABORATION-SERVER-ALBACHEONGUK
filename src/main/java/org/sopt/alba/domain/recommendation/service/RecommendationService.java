package org.sopt.alba.domain.recommendation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.exception.BusinessException;
import org.sopt.alba.domain.job.code.JobErrorCode;
import org.sopt.alba.domain.job.dto.response.JobSummaryResponse;
import org.sopt.alba.domain.job.entity.Region;
import org.sopt.alba.domain.job.repository.RegionRepository;
import org.sopt.alba.domain.recommendation.code.RecommendationErrorCode;
import org.sopt.alba.domain.recommendation.dto.response.RegionRecommendationResponse;
import org.sopt.alba.domain.recommendation.repository.RecommendationQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int REGION_RECOMMENDATION_SIZE = 8; // TODO 카드 개수 확정 필요
    private static final int SIDO_DEPTH = 1;

    private final RegionRepository regionRepository;
    private final RecommendationQueryRepository recommendationQueryRepository;
    private final Clock clock;

    //이력서 추천 로직도 넣기

    /* 시/도 기준으로 지역별 공고를 조회 */
    public RegionRecommendationResponse getRegionRecommendations(Long sidoId) {
        Region sido = regionRepository.findById(sidoId)
                .orElseThrow(() ->
                        new BusinessException(JobErrorCode.REGION_NOT_FOUND));

        if (sido.getDepth() != SIDO_DEPTH) {
            throw new BusinessException(RecommendationErrorCode.INVALID_SIDO);
        }

        LocalDate today = LocalDate.now(clock);

        List<JobSummaryResponse> jobs = recommendationQueryRepository
                .findOpenBySido(sidoId, today, REGION_RECOMMENDATION_SIZE)
                .stream()
                .map(posting -> JobSummaryResponse.of(posting, today))
                .toList();

        return new RegionRecommendationResponse(
                sido.getId(),
                sido.getName(),
                jobs
        );
    }
}