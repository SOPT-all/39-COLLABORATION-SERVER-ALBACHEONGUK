package org.sopt.alba.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.auth.CurrentMember;
import org.sopt.alba.common.dto.CommonResponse;
import org.sopt.alba.domain.member.code.MemberSuccessCode;
import org.sopt.alba.domain.member.dto.response.MemberProfileResponse;
import org.sopt.alba.domain.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 조회")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 프로필 조회", description = "X-Member-Id의 목업 회원 이름과 진행 중 공고 수를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<MemberProfileResponse>> getMyProfile(
            @CurrentMember Long memberId) {
        return CommonResponse.success(MemberSuccessCode.GET_MEMBER_PROFILE_SUCCESS,
                memberService.getMyProfile(memberId));
    }
}
