package org.sopt.alba.domain.job.enums;

/* 공고의 경력 조건 */
public enum CareerRequirement {
    ENTRY_OK,     // 신입 가능
    ANY,          // 경력무관
    REQUIRED,     // 필수 경력 (JobRequirement.careerMonths 이상)
    PREFERRED     // 우대 경력 (JobRequirement.careerMonths 이상이면 만점)
}