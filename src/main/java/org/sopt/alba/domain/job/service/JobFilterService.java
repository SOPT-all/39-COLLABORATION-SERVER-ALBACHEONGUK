package org.sopt.alba.domain.job.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.domain.job.dto.response.JobFilterOptionsResponse;
import org.sopt.alba.domain.job.dto.response.JobFilterOptionsResponse.CategoryOption;
import org.sopt.alba.domain.job.dto.response.JobFilterOptionsResponse.CodeOption;
import org.sopt.alba.domain.job.dto.response.JobFilterOptionsResponse.DistrictOption;
import org.sopt.alba.domain.job.dto.response.JobFilterOptionsResponse.RegionOption;
import org.sopt.alba.domain.job.dto.response.JobFilterOptionsResponse.SubcategoryOption;
import org.sopt.alba.domain.job.entity.JobCategory;
import org.sopt.alba.domain.job.entity.Region;
import org.sopt.alba.domain.job.enums.JobPayTypeCode;
import org.sopt.alba.domain.job.enums.JobWorkPeriodCode;
import org.sopt.alba.domain.job.enums.PayType;
import org.sopt.alba.domain.job.enums.Workday;
import org.sopt.alba.domain.job.repository.JobCategoryRepository;
import org.sopt.alba.domain.job.repository.RegionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobFilterService {

    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;

    public JobFilterOptionsResponse findOptions() {
        return new JobFilterOptionsResponse(
                findRegions(),
                findCategories(),
                Arrays.stream(JobWorkPeriodCode.values())
                        .map(period -> new CodeOption(period.name(), period.getLabel()))
                        .toList(),
                Arrays.stream(Workday.values())
                        .map(day -> new CodeOption(day.name(), day.getLabel()))
                        .toList(),
                Arrays.stream(PayType.values())
                        .map(pay -> new CodeOption(JobPayTypeCode.toApiCode(pay), pay.getLabel()))
                        .toList());
    }

    private List<RegionOption> findRegions() {
        List<Region> regions = regionRepository.findAll(Sort.by("id"));
        var districtsByRegion = regions.stream()
                .filter(region -> region.getParent() != null)
                .collect(groupingBy(region -> region.getParent().getId(),
                        mapping(region -> new DistrictOption(region.getId(), region.getName()), toList())));

        return regions.stream()
                .filter(region -> region.getParent() == null)
                .map(region -> new RegionOption(region.getId(), region.getName(),
                        districtsByRegion.getOrDefault(region.getId(), List.of())))
                .toList();
    }

    private List<CategoryOption> findCategories() {
        List<JobCategory> categories = jobCategoryRepository.findAll(Sort.by("code"));
        var subcategoriesByCategory = categories.stream()
                .filter(category -> category.getParent() != null)
                .collect(groupingBy(category -> category.getParent().getCode(),
                        mapping(category -> new SubcategoryOption(category.getCode(), category.getName()), toList())));

        return categories.stream()
                .filter(category -> category.getParent() == null)
                .map(category -> new CategoryOption(category.getCode(), category.getName(),
                        subcategoriesByCategory.getOrDefault(category.getCode(), List.of())))
                .toList();
    }
}
