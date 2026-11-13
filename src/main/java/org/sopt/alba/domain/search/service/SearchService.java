package org.sopt.alba.domain.search.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.exception.BusinessException;
import org.sopt.alba.domain.search.code.SearchErrorCode;
import org.sopt.alba.domain.search.dto.response.PopularKeywordResponse;
import org.sopt.alba.domain.search.dto.response.PopularKeywordsResponse;
import org.sopt.alba.domain.search.repository.PopularKeywordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final PopularKeywordRepository popularKeywordRepository;
    private final Clock clock;

    public PopularKeywordsResponse getPopularKeywords(int size) {
        if (size != 5 && size != 10) {
            throw new BusinessException(SearchErrorCode.INVALID_POPULAR_KEYWORD_SIZE);
        }

        List<PopularKeywordResponse> keywords = popularKeywordRepository
                .findAllByOrderByRankAsc(PageRequest.of(0, size)).stream()
                .map(PopularKeywordResponse::of)
                .toList();

        return new PopularKeywordsResponse(LocalDate.now(clock), keywords);
    }
}
