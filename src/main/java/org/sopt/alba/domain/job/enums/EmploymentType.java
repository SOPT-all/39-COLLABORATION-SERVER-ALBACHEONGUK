package org.sopt.alba.domain.job.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/* 근무형태 */

@Getter
@RequiredArgsConstructor
public enum EmploymentType {
    PART_TIME("아르바이트"), // (데이터) 더미 양식 표기
    FULL_TIME("정규직"),
    CONTRACT("계약직"),
    DISPATCH("파견직"),
    INTERN("인턴");

    private final String label;
}
