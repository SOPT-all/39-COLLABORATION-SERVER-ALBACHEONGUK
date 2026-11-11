package org.sopt.alba.domain.job.service;

import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.exception.BusinessException;
import org.sopt.alba.common.storage.SignedImageUrl;
import org.sopt.alba.common.storage.SupabaseStorageService;
import org.sopt.alba.domain.job.code.JobImageErrorCode;
import org.sopt.alba.domain.job.dto.response.JobImageResponse;
import org.sopt.alba.domain.job.entity.JobImage;
import org.sopt.alba.domain.job.repository.JobImageRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobImageService {
    private final JobImageRepository jobImageRepository;
    private final SupabaseStorageService storageService;

    public JobImageResponse getJobImage(Long jobPostingId) {
        JobImage image = jobImageRepository.findById(jobPostingId)
                .orElseThrow(() -> new BusinessException(JobImageErrorCode.JOB_POSTING_NOT_FOUND));
        if (image.getImageUrl() == null || image.getImageUrl().isBlank()) {
            throw new BusinessException(JobImageErrorCode.JOB_IMAGE_NOT_FOUND);
        }
        SignedImageUrl signed = storageService.createSignedUrl(image.getImageUrl());
        return new JobImageResponse(jobPostingId, signed.imageUrl(), signed.expiresAt());
    }
}
