package org.sopt.alba.domain.search.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sopt.alba.common.auth.CurrentMemberArgumentResolver;
import org.sopt.alba.common.config.ClockConfig;
import org.sopt.alba.domain.search.service.SearchService;
import org.sopt.alba.domain.search.entity.PopularKeyword;
import org.sopt.alba.domain.search.enums.KeywordTrend;
import org.sopt.alba.domain.search.repository.PopularKeywordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SearchController.class)
@Import({SearchService.class, CurrentMemberArgumentResolver.class, SearchControllerTest.FixedClockConfig.class})
class SearchControllerTest {

    private static final String URL = "/api/v1/search/popular-keywords";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PopularKeywordRepository popularKeywordRepository;

    @BeforeEach
    void setUp() {
        List<PopularKeyword> keywords = List.of(
                new PopularKeyword(1, "카페", KeywordTrend.STEADY, null),
                new PopularKeyword(2, "편의점", KeywordTrend.UP, null),
                new PopularKeyword(3, "주말", KeywordTrend.UP, null),
                new PopularKeyword(4, "베이커리", KeywordTrend.STEADY, null),
                new PopularKeyword(5, "물류", KeywordTrend.UP, null),
                new PopularKeyword(6, "단기", KeywordTrend.DOWN, null),
                new PopularKeyword(7, "바리스타", KeywordTrend.NEW, null),
                new PopularKeyword(8, "사무", KeywordTrend.DOWN, null),
                new PopularKeyword(9, "마트", KeywordTrend.STEADY, null),
                new PopularKeyword(10, "야간", KeywordTrend.NEW, null)
        );
        when(popularKeywordRepository.findAllByOrderByRankAsc(any(Pageable.class)))
                .thenAnswer(invocation -> keywords.subList(0, invocation.<Pageable>getArgument(0).getPageSize()));
    }

    @Test
    void returnsEmptyListWhenNoKeywordsAreStored() throws Exception {
        when(popularKeywordRepository.findAllByOrderByRankAsc(any(Pageable.class))).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.baseDate").value("2026.11.13"))
                .andExpect(jsonPath("$.data.keywords", hasSize(0)));
    }

    @Test
    void returnsTenKeywordsByDefaultWithoutLogin() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("GET_POPULAR_KEYWORDS_SUCCESS"))
                .andExpect(jsonPath("$.message").value("인기 검색어 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.baseDate").value("2026.11.13"))
                .andExpect(jsonPath("$.data.keywords", hasSize(10)))
                .andExpect(jsonPath("$.data.keywords[*].rank", contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)))
                .andExpect(jsonPath("$.data.keywords[*].keyword", contains(
                        "카페", "편의점", "주말", "베이커리", "물류", "단기", "바리스타", "사무", "마트", "야간")))
                .andExpect(jsonPath("$.data.keywords[*].trend", contains(
                        "SAME", "UP", "UP", "SAME", "UP", "DOWN", "NEW", "DOWN", "SAME", "NEW")));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10})
    void returnsRequestedNumberOfTopKeywords(int size) throws Exception {
        mockMvc.perform(get(URL).param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keywords", hasSize(size)))
                .andExpect(jsonPath("$.data.keywords[0].rank").value(1))
                .andExpect(jsonPath("$.data.keywords[4].keyword").value("물류"));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1, 4, 6, 9, 11, Integer.MAX_VALUE})
    void rejectsUnsupportedSize(int size) throws Exception {
        mockMvc.perform(get(URL).param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_POPULAR_KEYWORD_SIZE"))
                .andExpect(jsonPath("$.message").value("size는 5 또는 10이어야 합니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "5.5", "2147483648"})
    void rejectsNonIntegerSize(String size) throws Exception {
        mockMvc.perform(get(URL).param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER_TYPE"));
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        Clock clock() {
            // UTC는 11월 12일이지만 KST는 11월 13일인 시각으로 날짜 경계를 검증합니다.
            return Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ClockConfig.KST);
        }
    }
}
