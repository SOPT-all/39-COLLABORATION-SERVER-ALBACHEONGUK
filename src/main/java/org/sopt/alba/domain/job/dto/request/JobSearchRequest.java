package org.sopt.alba.domain.job.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.sopt.alba.domain.job.enums.Workday;

public record JobSearchRequest(
        String keyword,
        List<@NotNull @Positive Long> regionIds,
        List<@NotBlank String> categoryCodes,
        List<@NotNull Workday> workdays,
        @Schema(description = "시간대 기준이 확정되기 전까지 생략합니다. 값을 보내면 400을 반환합니다.")
        List<@NotBlank String> workTimeCodes,
        @Schema(
                description = "급여 유형 코드입니다. 시급 10,000원 이상은 payType=HOURLY와 minPay=10000을 함께 보냅니다.",
                allowableValues = {"HOURLY", "DAILY", "WEEKLY", "MONTHLY", "YEARLY", "PER_TASK"})
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
        workTimeCodes = workTimeCodes == null ? List.of() : workTimeCodes;
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
