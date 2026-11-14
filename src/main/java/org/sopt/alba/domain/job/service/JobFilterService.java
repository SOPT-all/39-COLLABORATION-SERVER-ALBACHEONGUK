package org.sopt.alba.domain.job.service;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.domain.job.dto.response.JobFilterOptionsResponse;
import org.sopt.alba.domain.job.dto.response.JobFilterOptionsResponse.HierarchyOption;
import org.sopt.alba.domain.job.dto.response.JobFilterOptionsResponse.NamedOption;
import org.sopt.alba.domain.job.enums.JobPayTypeCode;
import org.sopt.alba.domain.job.enums.PayType;
import org.sopt.alba.domain.job.enums.Workday;
import org.sopt.alba.domain.job.repository.JobCategoryRepository;
import org.sopt.alba.domain.job.repository.RegionRepository;
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
                // (데이터) 필터 옵션은 전체 급여 유형을 제공하고 API의 건별 코드는 PER_TASK로 통일한다.
                Arrays.stream(PayType.values())
                        .map(payType -> new NamedOption(JobPayTypeCode.toApiCode(payType), payType.getLabel()))
                        .toList(),
                Arrays.stream(Workday.values())
                        .map(day -> new NamedOption(day.name(), day.getLabel() + "요일"))
                        .toList());
    }

    private List<HierarchyOption> findRegions() {
        // 현재 DB에는 지역 ID만 있다. 지역 코드 매핑을 확정하기 전에는 검색에 쓸 수 있도록 ID를 문자열로 제공한다.
        return regionRepository.findAllWithParentOrderById().stream()
                .map(region -> new HierarchyOption(region.getId().toString(), region.getName(),
                        region.getParent() == null ? null : region.getParent().getId().toString()))
                .toList();
    }

    private List<HierarchyOption> findCategories() {
        return jobCategoryRepository.findAllWithParentOrderByCode().stream()
                .map(category -> new HierarchyOption(category.getCode(), category.getName(),
                        category.getParent() == null ? null : category.getParent().getCode()))
                .toList();
    }
}
