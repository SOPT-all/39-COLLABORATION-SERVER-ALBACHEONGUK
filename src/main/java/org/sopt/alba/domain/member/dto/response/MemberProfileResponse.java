package org.sopt.alba.domain.member.dto.response;

import org.sopt.alba.domain.member.entity.Member;

public record MemberProfileResponse(
        String name,
        long ongoingJobCount // (데이터) 메인 프로필 카드의 진행 중 공고 수
) {

    public static MemberProfileResponse of(Member member, long ongoingJobCount) {
        return new MemberProfileResponse(member.getName(), ongoingJobCount);
    }
}
