package org.sopt.alba.domain.job.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.dto.CommonResponse;
import org.sopt.alba.domain.job.code.JobImageSuccessCode;
import org.sopt.alba.domain.job.dto.response.JobImageResponse;
import org.sopt.alba.domain.job.service.JobImageService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Job images", description = "공고 이미지 조회")
@RestController
@RequestMapping("/api/v1/job-postings")
@RequiredArgsConstructor
public class JobImageController {
    private final JobImageService jobImageService;

    @Operation(summary = "공고 이미지 서명 URL 발급",
            description = "imageUrl을 img의 src로 사용합니다. 기본 유효시간은 10분이며 만료 후 다시 발급합니다.")
    @GetMapping("/{jobPostingId}/image")
    public ResponseEntity<CommonResponse<JobImageResponse>> getJobImage(
            @PathVariable @Positive(message = "공고 ID는 1 이상이어야 합니다.") Long jobPostingId) {
        ResponseEntity<CommonResponse<JobImageResponse>> response = CommonResponse.success(
                JobImageSuccessCode.GET_JOB_IMAGE_SUCCESS, jobImageService.getJobImage(jobPostingId));
        return ResponseEntity.status(response.getStatusCode()).cacheControl(CacheControl.noStore())
                .body(response.getBody());
    }
}
