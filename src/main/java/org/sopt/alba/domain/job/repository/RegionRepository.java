package org.sopt.alba.domain.job.repository;

import java.util.List;
import org.sopt.alba.domain.job.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findAllByDepthOrderByIdAsc(int depth);

    List<Region> findAllByParentIdOrderByIdAsc(Long parentId);
}