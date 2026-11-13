package org.sopt.alba.domain.job.repository;

import java.util.List;
import org.sopt.alba.domain.job.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findAllByDepthOrderByIdAsc(int depth);

    List<Region> findAllByParentIdOrderByIdAsc(Long parentId);

    @Query("select r from Region r left join fetch r.parent order by r.id")
    List<Region> findAllWithParentOrderById();
}
