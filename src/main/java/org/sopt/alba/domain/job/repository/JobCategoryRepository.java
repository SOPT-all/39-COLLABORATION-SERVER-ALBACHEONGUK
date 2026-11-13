package org.sopt.alba.domain.job.repository;

import java.util.List;
import org.sopt.alba.domain.job.entity.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobCategoryRepository extends JpaRepository<JobCategory, String> {

    @Query("select c from JobCategory c left join fetch c.parent order by c.code")
    List<JobCategory> findAllWithParentOrderByCode();
}
