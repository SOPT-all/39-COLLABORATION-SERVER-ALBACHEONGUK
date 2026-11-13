package org.sopt.alba.domain.job.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.dto.CommonResponse;
import org.sopt.alba.domain.job.code.JobSuccessCode;
import org.sopt.alba.domain.job.dto.response.JobFilterOptionsResponse;
import org.sopt.alba.domain.job.service.JobFilterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공고")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobFilterService jobFilterService;

    @Operation(summary = "필터 옵션 조회")
    @GetMapping("/filters")
    public ResponseEntity<CommonResponse<JobFilterOptionsResponse>> findFilterOptions() {
        return CommonResponse.success(JobSuccessCode.GET_JOB_FILTERS_SUCCESS, jobFilterService.findOptions());
    }
}
