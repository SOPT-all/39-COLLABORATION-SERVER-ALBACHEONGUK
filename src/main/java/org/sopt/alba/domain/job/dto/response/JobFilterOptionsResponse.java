package org.sopt.alba.domain.job.dto.response;

import java.util.List;

public record JobFilterOptionsResponse(
        List<HierarchyOption> regions,
        List<HierarchyOption> categories,
        List<NamedOption> payConditions,
        List<NamedOption> workdays
) {
    public record HierarchyOption(String code, String name, String parentCode) {
    }

    public record NamedOption(String code, String name) {
    }
}
