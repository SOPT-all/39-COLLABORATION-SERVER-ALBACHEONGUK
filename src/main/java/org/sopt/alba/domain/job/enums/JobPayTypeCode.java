package org.sopt.alba.domain.job.enums;

public final class JobPayTypeCode {

    private JobPayTypeCode() {
    }

    public static String toApiCode(PayType payType) {
        return payType == PayType.PER_CASE ? "PER_TASK" : payType.name();
    }
}
