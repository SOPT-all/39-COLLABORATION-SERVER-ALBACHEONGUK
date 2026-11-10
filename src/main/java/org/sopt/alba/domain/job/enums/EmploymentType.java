package org.sopt.alba.domain.job.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/* 근무형태 */
// TODO 기획 더미 시트의 근무형태 종류가 확정되면 항목을 맞춘다.

@Getter
@RequiredArgsConstructor
public enum EmploymentType {
    PART_TIME("알바"),
    FULL_TIME("정규직"),
    CONTRACT("계약직"),
    DISPATCH("파견직"),
    INTERN("인턴");

    private final String label;
}