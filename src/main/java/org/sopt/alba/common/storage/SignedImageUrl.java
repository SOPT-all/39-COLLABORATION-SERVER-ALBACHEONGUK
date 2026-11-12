package org.sopt.alba.common.storage;

import java.time.Instant;

public record SignedImageUrl(String imageUrl, Instant expiresAt) {
}
