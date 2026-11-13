package org.sopt.alba.domain.job.enums;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobWorkPeriodCode {
    ONE_DAY("ONE_DAY", "1일"),
    UNDER_1_WEEK("UNDER_ONE_WEEK", "1주일 이하"),
    WEEK_TO_1_MONTH("ONE_WEEK_TO_ONE_MONTH", "1주일~1개월"),
    MONTH_1_TO_3("ONE_TO_THREE_MONTHS", "1개월~3개월"),
    MONTH_3_TO_6("THREE_TO_SIX_MONTHS", "3개월~6개월"),
    MONTH_6_TO_1_YEAR("SIX_MONTHS_TO_ONE_YEAR", "6개월~1년"),
    OVER_1_YEAR("OVER_ONE_YEAR", "1년 이상");

    private final String storedCode;
    private final String label;

    public static String labelOf(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(period -> period.storedCode.equals(value) || period.name().equals(value))
                .findFirst()
                .map(JobWorkPeriodCode::getLabel)
                .orElse(value);
    }
}
