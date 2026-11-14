package org.sopt.alba.domain.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.dto.CommonResponse;
import org.sopt.alba.domain.search.code.SearchSuccessCode;
import org.sopt.alba.domain.search.dto.response.PopularKeywordsResponse;
import org.sopt.alba.domain.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search", description = "검색어 조회")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "인기 검색어 조회", description = "기획 목업 순위를 반환합니다. 실시간 집계 없이 로그인 없이 조회할 수 있습니다.") // (데이터)
    @GetMapping("/popular-keywords")
    public ResponseEntity<CommonResponse<PopularKeywordsResponse>> getPopularKeywords(
            @Parameter(description = "메인 화면은 5개, 검색 결과 화면은 10개. 기본 10개",
                    schema = @Schema(allowableValues = {"5", "10"}, defaultValue = "10"))
            @RequestParam(defaultValue = "10") int size) {
        return CommonResponse.success(SearchSuccessCode.GET_POPULAR_KEYWORDS_SUCCESS,
                searchService.getPopularKeywords(size));
    }
}
