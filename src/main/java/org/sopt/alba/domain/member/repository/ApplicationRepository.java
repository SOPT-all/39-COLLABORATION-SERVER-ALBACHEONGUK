package org.sopt.alba.domain.member.repository;

import java.util.List;
import org.sopt.alba.domain.member.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("select a.jobPosting.id from Application a where a.member.id = :memberId")
    List<Long> findAppliedJobPostingIds(@Param("memberId") Long memberId);
}