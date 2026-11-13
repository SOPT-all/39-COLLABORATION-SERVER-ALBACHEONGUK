package org.sopt.alba.domain.search.dto.response;

import org.sopt.alba.domain.search.enums.KeywordTrend;
import org.sopt.alba.domain.search.entity.PopularKeyword;

public record PopularKeywordResponse(
        int rank,
        String keyword,
        KeywordTrend trend
) {
    public static PopularKeywordResponse of(PopularKeyword popularKeyword) {
        return new PopularKeywordResponse(popularKeyword.getRank(), popularKeyword.getKeyword(),
                popularKeyword.getTrend());
    }
}
