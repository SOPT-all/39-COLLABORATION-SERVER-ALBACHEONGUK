package org.sopt.alba.domain.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.dto.CommonResponse;
import org.sopt.alba.domain.recommendation.code.RecommendationSuccessCode;
import org.sopt.alba.domain.recommendation.dto.response.RegionRecommendationResponse;
import org.sopt.alba.domain.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recommendation", description = "지역별 공고")
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
            summary = "지역별 공고 조회",
            description = "시/도 ID를 기준으로 마감되지 않은 지역 공고를 조회합니다."
    )
    @GetMapping("/region")
    public ResponseEntity<CommonResponse<RegionRecommendationResponse>>
    getRegionRecommendations(
            @Parameter(description = "시/도 ID", example = "1", required = true)
            @RequestParam
            @Positive(message = "sidoId는 1 이상이어야 합니다.")
            Long sidoId
    ) {
        return CommonResponse.success(
                RecommendationSuccessCode.GET_REGION_RECOMMENDATIONS_SUCCESS,
                recommendationService.getRegionRecommendations(sidoId)
        );
    }
}