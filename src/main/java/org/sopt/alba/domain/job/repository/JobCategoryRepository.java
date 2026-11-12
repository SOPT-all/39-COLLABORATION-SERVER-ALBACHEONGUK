package org.sopt.alba.domain.job.repository;

import org.sopt.alba.domain.job.entity.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobCategoryRepository extends JpaRepository<JobCategory, String> {
}