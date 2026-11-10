package org.sopt.alba.domain.job.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.sopt.alba.domain.job.enums.ExposureType;
import org.sopt.alba.domain.job.enums.PayType;
import org.sopt.alba.domain.job.enums.Workday;

public record JobSummaryResponse(
        Long jobId,
        ExposureType exposureType,
        String title,
        String companyName,
        String imageUrl,
        String regionName,
        String dong,
        String nearbySubway,
        String categoryName,
        PayType payType,
        int payAmount,
        String workPeriod,
        List<Workday> workdays,
        boolean daysNegotiable,
        LocalTime startTime,
        LocalTime endTime,
        boolean timeNegotiable,
        String applyMethod,
        LocalDate deadline,
        boolean closed,
        String postedLabel
) {
}