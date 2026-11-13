package org.sopt.alba.domain.job.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayType {
    HOURLY("시급"),
    DAILY("일급"),
    WEEKLY("주급"),
    MONTHLY("월급"),
    YEARLY("연봉"),
    PER_CASE("건별");

    private final String label;
}
