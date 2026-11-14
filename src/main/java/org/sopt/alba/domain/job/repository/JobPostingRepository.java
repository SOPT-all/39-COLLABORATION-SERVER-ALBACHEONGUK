package org.sopt.alba.domain.job.repository;

import java.time.LocalDate;
import org.sopt.alba.domain.job.entity.JobPosting;
import org.sopt.alba.domain.job.enums.RecruitmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // (데이터) 채용 완료되지 않았고 KST 오늘 기준 마감일이 지나지 않은 공고 수.
    long countByRecruitmentStatusAndDeadlineIsNull(RecruitmentStatus status);

    long countByRecruitmentStatusAndDeadlineGreaterThanEqual(RecruitmentStatus status, LocalDate today);
}