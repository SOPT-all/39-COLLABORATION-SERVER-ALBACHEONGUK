package org.sopt.alba.domain.search.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record PopularKeywordsResponse(
        @JsonFormat(pattern = "yyyy.MM.dd")
        @Schema(type = "string", example = "2026.11.13", description = "한국 시간 기준 오늘 날짜")
        LocalDate baseDate,
        List<PopularKeywordResponse> keywords
) {
}
