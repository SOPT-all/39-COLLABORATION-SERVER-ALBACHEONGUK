package org.sopt.alba.domain.job.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import org.sopt.alba.domain.job.entity.JobPosting;
import org.sopt.alba.domain.job.entity.Region;
import org.sopt.alba.domain.job.enums.ExposureType;
import org.sopt.alba.domain.job.enums.PayType;
import org.sopt.alba.domain.job.enums.Workday;

/* 공고 목록 카드 1개 (상세 조회 없음 → 카드에 보이는 정보만) */
public record JobSummaryResponse(
        Long jobId,
        ExposureType exposureType,
        String title,
        String companyName,
        String imageUrl,
        String regionName,         // 예: "서울 강남구"
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
        String postedLabel         // 오늘 / n일 전 / n달 전 / n년 전
) {

    public static JobSummaryResponse of(JobPosting posting, LocalDate today) {
        Region sigungu = posting.getRegion();
        String regionName = sigungu.getParent() == null
                ? sigungu.getName()
                : sigungu.getParent().getName() + " " + sigungu.getName();

        return new JobSummaryResponse(
                posting.getId(),
                posting.getExposureType(),
                posting.getTitle(),
                posting.getCompanyName(),
                posting.getImageUrl(),
                regionName,
                posting.getDong(),
                posting.getNearbySubway(),
                posting.getJobCategory().getName(),
                posting.getPayType(),
                posting.getPayAmount(),
                posting.getWorkPeriod(),
                posting.getWorkdays().stream().sorted(Comparator.naturalOrder()).toList(),   // 월→일 순
                posting.isDaysNegotiable(),
                posting.getStartTime(),
                posting.getEndTime(),
                posting.isTimeNegotiable(),
                posting.getApplyMethod(),
                posting.getDeadline(),
                posting.isClosed(today),
                postedLabel(posting.getPublishedAt(), today)
        );
    }

    static String postedLabel(LocalDate publishedAt, LocalDate today) {
        long days = ChronoUnit.DAYS.between(publishedAt, today);
        if (days <= 0) {
            return "오늘";
        }
        if (days <= 30) {
            return days + "일 전";
        }
        long months = days / 30;
        if (months < 12) {
            return months + "달 전";
        }
        return (months / 12) + "년 전";
    }
}