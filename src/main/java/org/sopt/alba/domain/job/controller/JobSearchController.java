package org.sopt.alba.domain.job.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.GlobalErrorCode;
import org.sopt.alba.common.dto.CommonResponse;
import org.sopt.alba.common.exception.BusinessException;
import org.sopt.alba.domain.job.code.JobSuccessCode;
import org.sopt.alba.domain.job.dto.request.JobSearchRequest;
import org.sopt.alba.domain.job.dto.response.JobSearchResponse;
import org.sopt.alba.domain.job.service.JobSearchService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공고")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jobs")
public class JobSearchController {

    private static final List<String> SEARCH_PARAMETERS = List.of(
            "keyword", "regionIds", "categoryCodes", "workdays", "workTimeCodes",
            "payType", "minPay", "maxPay", "ongoing", "sort", "page", "size");
    private static final Set<String> MULTI_VALUE_PARAMETERS =
            Set.of("regionIds", "categoryCodes", "workdays", "workTimeCodes");

    private final JobSearchService jobSearchService;

    @Operation(summary = "공고 검색 목록 조회")
    @ApiResponse(responseCode = "200", description = "공고 목록 조회에 성공했습니다.")
    @ApiResponse(responseCode = "400", description = "잘못된 입력값 또는 파라미터 형식")
    @GetMapping
    public ResponseEntity<CommonResponse<JobSearchResponse>> findJobs(
            @Valid @ModelAttribute @ParameterObject JobSearchRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest
    ) {
        if (bindingResult.getFieldErrors().stream().anyMatch(FieldError::isBindingFailure)) {
            throw new BusinessException(GlobalErrorCode.INVALID_PARAMETER_TYPE);
        }
        if (bindingResult.hasErrors() || hasInvalidSearchParameter(httpRequest)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        return CommonResponse.success(JobSuccessCode.GET_JOBS_SUCCESS, jobSearchService.findJobs(request));
    }

    private boolean hasInvalidSearchParameter(HttpServletRequest request) {
        for (String parameter : SEARCH_PARAMETERS) {
            String[] values = request.getParameterValues(parameter);
            if (values == null) {
                continue;
            }
            if (values.length > 1 && !MULTI_VALUE_PARAMETERS.contains(parameter)) {
                return true;
            }
            if (!"keyword".equals(parameter) && Arrays.stream(values).anyMatch(String::isBlank)) {
                return true;
            }
        }
        return false;
    }
}
