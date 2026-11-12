package org.sopt.alba.domain.job.repository;

import org.sopt.alba.domain.job.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
}