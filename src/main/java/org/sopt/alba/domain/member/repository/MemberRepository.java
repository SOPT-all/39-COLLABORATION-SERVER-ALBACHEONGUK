package org.sopt.alba.domain.member.repository;

import org.sopt.alba.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}