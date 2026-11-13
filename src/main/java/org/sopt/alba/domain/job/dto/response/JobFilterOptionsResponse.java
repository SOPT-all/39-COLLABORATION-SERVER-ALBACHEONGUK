package org.sopt.alba.domain.job.dto.response;

import java.util.List;

public record JobFilterOptionsResponse(
        List<RegionOption> regions,
        List<CategoryOption> categories,
        List<CodeOption> workPeriods,
        List<CodeOption> workDays,
        List<CodeOption> payTypes
) {
    public record RegionOption(Long id, String name, List<DistrictOption> children) {
    }

    public record DistrictOption(Long id, String name) {
    }

    public record CategoryOption(String code, String name, List<SubcategoryOption> children) {
    }

    public record SubcategoryOption(String code, String name) {
    }

    public record CodeOption(String code, String label) {
    }
}
