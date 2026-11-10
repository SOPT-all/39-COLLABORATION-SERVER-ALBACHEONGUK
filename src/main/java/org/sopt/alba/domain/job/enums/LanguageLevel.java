package org.sopt.alba.domain.job.enums;

/* 외국어 수준. 선언 순서가 낮은 수준에서 높은 수준으로 */
public enum LanguageLevel {
    BASIC,          // 일상 회화
    INTERMEDIATE,   // 업무 회화
    ADVANCED;       // 능숙

    public boolean isAtLeast(LanguageLevel required) {
        return this.ordinal() >= required.ordinal();
    }
}