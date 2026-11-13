package org.sopt.alba.domain.job.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.sopt.alba.domain.job.dto.request.JobSearchRequest;
import org.sopt.alba.domain.job.dto.request.JobSearchRequest.SortType;
import org.sopt.alba.domain.job.entity.JobPosting;
import org.sopt.alba.domain.job.entity.Region;
import org.sopt.alba.domain.job.enums.ExposureType;
import org.sopt.alba.domain.job.enums.JobPayTypeCode;
import org.sopt.alba.domain.job.enums.WorkPeriod;
import org.sopt.alba.domain.job.enums.Workday;
import org.springframework.data.domain.Page;

public record JobSearchResponse(
        String keyword,
        AppliedFilters filters,
        long totalCount,
        SortType sort,
        int page,
        int size,
        int totalPages,
        List<JobGroup> groups
) {

    public static JobSearchResponse of(JobSearchRequest request, Page<JobPosting> result, LocalDate today) {
        Map<ExposureType, List<JobItem>> jobsByExposureType = new EnumMap<>(ExposureType.class);
        for (JobPosting posting : result.getContent()) {
            jobsByExposureType.computeIfAbsent(posting.getExposureType(), key -> new ArrayList<>())
                    .add(JobItem.of(posting, today));
        }
        List<JobGroup> groups = List.of(
                new JobGroup(ExposureType.PREMIUM, jobsByExposureType.getOrDefault(ExposureType.PREMIUM, List.of())),
                new JobGroup(ExposureType.LIGHT, jobsByExposureType.getOrDefault(ExposureType.LIGHT, List.of())),
                new JobGroup(ExposureType.NORMAL, jobsByExposureType.getOrDefault(ExposureType.NORMAL, List.of())));
        return new JobSearchResponse(request.keyword(), AppliedFilters.of(request), result.getTotalElements(),
                request.sort(), result.getNumber() + 1, result.getSize(), result.getTotalPages(), groups);
    }

    public record AppliedFilters(
            List<Long> regionIds,
            List<String> categoryCodes,
            List<Workday> workdays,
            String payType,
            Integer minPay,
            Integer maxPay,
            boolean ongoing
    ) {
        private static AppliedFilters of(JobSearchRequest request) {
            return new AppliedFilters(request.regionIds(), request.categoryCodes(), request.workdays(),
                    request.payType(), request.minPay(), request.maxPay(),
                    Boolean.parseBoolean(request.ongoing()));
        }
    }

    public record JobGroup(ExposureType exposureType, List<JobItem> jobs) {
    }

    public record JobItem(
            Long jobId,
            String title,
            String companyName,
            String regionName,
            String dong,
            String fullAddress,
            String nearbySubway,
            String categoryName,
            String payType,
            Integer payAmount,
            String convertedPayType,
            Integer convertedPayAmount,
            boolean payConversionAvailable,
            List<Workday> workdays,
            LocalTime startTime,
            LocalTime endTime,
            String workPeriod,
            String applyMethod,
            LocalDate deadline,
            Integer dDay,
            boolean closed,
            boolean scrapped
    ) {
        private static JobItem of(JobPosting posting, LocalDate today) {
            LocalDate deadline = posting.getDeadline();
            Integer daysUntilDeadline = deadline == null ? null : Math.toIntExact(ChronoUnit.DAYS.between(today, deadline));
            WorkPeriod workPeriod = posting.getWorkPeriod();
            String nearbySubway = posting.getNearbySubway();
            return new JobItem(
                    posting.getId(),
                    posting.getTitle(),
                    posting.getCompanyName(),
                    posting.getRegion().getFullName(),
                    posting.getDong(),
                    fullAddress(posting),
                    nearbySubway == null || nearbySubway.isBlank() ? null : nearbySubway.strip(),
                    posting.getJobCategory().getName(),
                    JobPayTypeCode.toApiCode(posting.getPayType()),
                    posting.getPayAmount() == 0 ? null : posting.getPayAmount(),
                    null,
                    null,
                    false,
                    posting.isDaysNegotiable() ? List.of() : posting.getWorkdays().stream().sorted().toList(),
                    posting.isTimeNegotiable() ? null : posting.getStartTime(),
                    posting.isTimeNegotiable() ? null : posting.getEndTime(),
                    workPeriod == null ? null : workPeriod.getLabel(),
                    posting.getApplyMethod(),
                    deadline,
                    daysUntilDeadline,
                    posting.isClosed(today),
                    false);
        }

        private static String fullAddress(JobPosting posting) {
            Region district = posting.getRegion();
            String province = district.getParent() == null ? null : district.getParent().getName();
            String districtName = district.getName();
            String dong = posting.getDong();
            String address = posting.getAddressDetail() == null ? "" : posting.getAddressDetail().strip();

            if (startsWithName(address, province)) {
                return address;
            }
            if (startsWithName(address, districtName)) {
                return joinAddress(province, address);
            }
            if (startsWithName(address, dong)) {
                return joinAddress(province, districtName, address);
            }
            return joinAddress(province, districtName, dong, address);
        }

        private static boolean startsWithName(String address, String name) {
            return name != null && !name.isBlank()
                    && (address.equals(name) || address.startsWith(name + " "));
        }

        private static String joinAddress(String... parts) {
            return Arrays.stream(parts)
                    .filter(part -> part != null && !part.isBlank())
                    .map(String::strip)
                    .collect(Collectors.joining(" "));
        }
    }
}
