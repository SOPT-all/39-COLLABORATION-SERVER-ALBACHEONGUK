package org.sopt.alba.domain.member.dto.response;

import org.sopt.alba.domain.member.entity.Member;

public record MemberProfileResponse(
        String name
) {

    public static MemberProfileResponse of(Member member) {
        return new MemberProfileResponse(member.getName());
    }
}
