package org.sopt.alba.domain.job.repository;

import java.util.Optional;
import org.sopt.alba.domain.job.entity.JobImage;
import org.springframework.data.repository.Repository;

public interface JobImageRepository extends Repository<JobImage, Long> {
    Optional<JobImage> findById(Long jobPostingId);
}
