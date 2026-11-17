package org.sopt.alba.domain.job.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.sopt.alba.domain.job.enums.Workday;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record JobSearchRequest(
        @Schema(description = "검색어. 앞뒤 공백 제거 후 최대 30자이며 특수문자는 검색어 매칭에서 제외됩니다.", maxLength = 30)
        String keyword,
        List<@NotNull @Positive Long> regionIds,
        List<@NotBlank String> categoryCodes,
        List<@NotNull Workday> workdays,
        @Min(1)
        @Max(7)
        @Schema(description = "주당 근무일수. workDays에 등록된 요일 개수와 일치하는 공고만 조회합니다.", example = "2")
        Integer daysPerWeek,
        String payType,
        @PositiveOrZero
        @Schema(description = "선택한 급여 유형의 최소 금액입니다. payType을 함께 보내야 합니다.")
        Integer minPay,
        @PositiveOrZero
        @Schema(description = "선택한 급여 유형의 최대 금액입니다. payType을 함께 보내야 합니다.")
        Integer maxPay,
        @Schema(type = "boolean", defaultValue = "false")
        String ongoing,
        @Schema(defaultValue = "ACCURACY")
        SortType sort,
        @Positive
        @Schema(defaultValue = "1")
        Integer page,
        @Schema(defaultValue = "20", allowableValues = {"20", "30", "50"})
        Integer size
) {
    public JobSearchRequest {
        keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
        regionIds = regionIds == null ? List.of() : regionIds;
        categoryCodes = categoryCodes == null ? List.of() : categoryCodes;
        workdays = workdays == null ? List.of() : workdays;
        ongoing = ongoing == null ? "false" : ongoing;
        sort = sort == null ? SortType.ACCURACY : sort;
        page = page == null ? 1 : page;
        size = size == null ? 20 : size;
    }

    public enum SortType {
        ACCURACY,
        PUBLISHED_DESC
    }
}
