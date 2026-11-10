package org.sopt.alba.domain.member.repository;

import java.util.List;
import org.sopt.alba.domain.member.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findAllByMemberIdOrderByModifiedAtDescIdDesc(Long memberId);
}