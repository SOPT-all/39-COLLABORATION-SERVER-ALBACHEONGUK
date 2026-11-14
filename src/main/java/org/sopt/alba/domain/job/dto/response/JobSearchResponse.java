package org.sopt.alba.domain.job.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
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
            LocalDate publishedAt, // (데이터) 마감 D-day 대신 등록일을 기준으로 표시한다.
            String postedLabel, // (데이터) 오늘/n일 전/n달 전/n년 전 라벨
            LocalDate deadline,
            String deadlineLabel, // (데이터) 날짜/상시모집/마감 상태 표시
            boolean daysNegotiable, // (데이터) 요일 협의 여부
            boolean timeNegotiable, // (데이터) 시간 협의 여부
            boolean closed
    ) {
        private static JobItem of(JobPosting posting, LocalDate today) {
            LocalDate deadline = posting.getDeadline();
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
                    posting.getPayAmount() == null || posting.getPayAmount() <= 0 ? null : posting.getPayAmount(), // (데이터)
                    null,
                    null,
                    false,
                    posting.isDaysNegotiable() ? List.of() : posting.getWorkdays().stream().sorted().toList(),
                    posting.isTimeNegotiable() ? null : posting.getStartTime(),
                    posting.isTimeNegotiable() ? null : posting.getEndTime(),
                    workPeriod == null ? null : workPeriod.getLabel(),
                    posting.getApplyMethod(),
                    posting.getPublishedAt(), // (데이터) 마감일까지 남은 날짜 대신 공고 등록일을 반환한다.
                    JobSummaryResponse.postedLabel(posting.getPublishedAt(), today), // (데이터)
                    deadline,
                    posting.isClosed(today) ? "마감" : deadline == null ? "상시모집"
                            : deadline.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd")) + " 마감", // (데이터)
                    posting.isDaysNegotiable(), // (데이터) 협의 문구를 프론트가 표시할 수 있게 한다.
                    posting.isTimeNegotiable(), // (데이터)
                    posting.isClosed(today));
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
