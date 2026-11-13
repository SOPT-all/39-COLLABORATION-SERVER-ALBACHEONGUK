package org.sopt.alba.domain.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.auth.CurrentMember;
import org.sopt.alba.common.dto.CommonResponse;
import org.sopt.alba.domain.recommendation.code.RecommendationSuccessCode;
import org.sopt.alba.domain.recommendation.dto.request.ResumeRecommendationRequest;
import org.sopt.alba.domain.recommendation.dto.response.RegionRecommendationResponse;
import org.sopt.alba.domain.recommendation.dto.response.ResumeRecommendationResponse;
import org.sopt.alba.domain.recommendation.service.RecommendationService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recommendation", description = "지역별 공고")
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
            summary = "지역별 공고 조회",
            description = "sido ID를 기준으로 모집 중이며 마감되지 않은 지역 공고를 최신 등록순 최대 4건 조회합니다."
    )
    @GetMapping("/region")
    public ResponseEntity<CommonResponse<RegionRecommendationResponse>>
    getRegionRecommendations(
            @Parameter(description = "시·도 ID", example = "1", required = true)
            @RequestParam("sido")
            @Positive(message = "sido는 1 이상이어야 합니다.")
            Long sidoId
    ) {
        return CommonResponse.success(
                RecommendationSuccessCode.GET_REGION_RECOMMENDATIONS_SUCCESS,
                recommendationService.getRegionRecommendations(sidoId)
        );
    }

    @Operation(
            summary = "이력서 매칭 공고 추천",
            description = "선택 이력서와 맞는 공고를 점수순으로 최대 3건 조회합니다. "
                    + "X-Member-Id 헤더가 없으면 status=GUEST. resumeId를 바꾸면 그 이력서 기준으로 다시 추천합니다. "
                    + "검색 결과 화면에서는 검색 조건(keyword, regionIds, categoryCodes, workdays, workPeriod, payType)을 같이 보냅니다."
    )
    @GetMapping("/resume")
    public ResponseEntity<CommonResponse<ResumeRecommendationResponse>>
    getResumeRecommendations(
            @CurrentMember(required = false) Long memberId,
            @ParameterObject @Valid @ModelAttribute ResumeRecommendationRequest request
    ) {
        return CommonResponse.success(
                RecommendationSuccessCode.GET_RESUME_RECOMMENDATIONS_SUCCESS,
                recommendationService.getResumeRecommendations(memberId, request)
        );
    }
}
