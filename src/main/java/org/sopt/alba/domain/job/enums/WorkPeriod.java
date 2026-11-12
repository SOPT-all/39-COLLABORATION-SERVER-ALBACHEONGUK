package org.sopt.alba.domain.job.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkPeriod {
    ONE_DAY("1일"),
    UNDER_ONE_WEEK("1주일 이하"),
    ONE_WEEK_TO_ONE_MONTH("1주일~1개월"),
    ONE_TO_THREE_MONTHS("1개월~3개월"),
    THREE_TO_SIX_MONTHS("3개월~6개월"),
    SIX_MONTHS_TO_ONE_YEAR("6개월~1년"),
    OVER_ONE_YEAR("1년 이상");

    private final String label;

    public boolean isAdjacentTo(WorkPeriod other) {
        return Math.abs(this.ordinal() - other.ordinal()) == 1;
    }
}