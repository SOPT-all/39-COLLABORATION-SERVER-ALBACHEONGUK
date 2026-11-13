package org.sopt.alba.domain.search.repository;

import java.util.List;
import org.sopt.alba.domain.search.entity.PopularKeyword;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularKeywordRepository extends JpaRepository<PopularKeyword, Integer> {

    List<PopularKeyword> findAllByOrderByRankAsc(Pageable pageable);
}
