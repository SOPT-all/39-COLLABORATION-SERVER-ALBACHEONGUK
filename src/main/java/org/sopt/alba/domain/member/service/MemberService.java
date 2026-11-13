package org.sopt.alba.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.exception.BusinessException;
import org.sopt.alba.domain.member.code.MemberErrorCode;
import org.sopt.alba.domain.member.dto.response.MemberProfileResponse;
import org.sopt.alba.domain.member.entity.Member;
import org.sopt.alba.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberProfileResponse getMyProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        return MemberProfileResponse.of(member);
    }
}
