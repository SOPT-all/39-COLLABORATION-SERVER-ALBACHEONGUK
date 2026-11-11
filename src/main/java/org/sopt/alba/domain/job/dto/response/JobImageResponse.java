package org.sopt.alba.domain.job.dto.response;

import java.time.Instant;

public record JobImageResponse(Long jobPostingId, String imageUrl, Instant expiresAt) {
}
