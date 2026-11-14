package org.sopt.alba.domain.member.service;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.exception.BusinessException;
import org.sopt.alba.domain.member.code.MemberErrorCode;
import org.sopt.alba.domain.member.dto.response.MemberProfileResponse;
import org.sopt.alba.domain.member.entity.Member;
import org.sopt.alba.domain.job.enums.RecruitmentStatus;
import org.sopt.alba.domain.job.repository.JobPostingRepository;
import org.sopt.alba.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final JobPostingRepository jobPostingRepository; // (데이터)
    private final Clock clock; // (데이터) KST 기준 진행 상태 계산

    public MemberProfileResponse getMyProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        LocalDate today = LocalDate.now(clock); // (데이터)
        long ongoingCount = jobPostingRepository.countByRecruitmentStatusAndDeadlineIsNull(RecruitmentStatus.RECRUITING)
                + jobPostingRepository.countByRecruitmentStatusAndDeadlineGreaterThanEqual(
                        RecruitmentStatus.RECRUITING, today); // (데이터)
        return MemberProfileResponse.of(member, ongoingCount); // (데이터)
    }
}
