package org.sopt.alba.domain.search.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sopt.alba.domain.search.entity.PopularKeyword;
import org.sopt.alba.domain.search.enums.KeywordTrend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

@DataJpaTest(properties = {
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:db/schema.sql",
        "spring.datasource.url=jdbc:h2:mem:popular-keywords;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PopularKeywordRepositoryTest {

    @Autowired
    private PopularKeywordRepository popularKeywordRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void readsStoredRanksInAscendingOrderAndLimitsTheResult() {
        for (int rank = 10; rank >= 1; rank--) {
            entityManager.persist(new PopularKeyword(rank, "검색어" + rank, KeywordTrend.STEADY, null));
        }
        entityManager.flush();
        entityManager.clear();

        List<PopularKeyword> keywords = popularKeywordRepository.findAllByOrderByRankAsc(PageRequest.of(0, 5));

        assertThat(keywords).extracting(PopularKeyword::getRank).containsExactly(1, 2, 3, 4, 5);
        assertThat(keywords).extracting(PopularKeyword::getTrend).containsOnly(KeywordTrend.STEADY);
    }

    @Test
    void returnsOnlyStoredKeywordsWhenFewerThanRequested() {
        entityManager.persist(new PopularKeyword(7, "바리스타", KeywordTrend.NEW, null));
        entityManager.persist(new PopularKeyword(2, "편의점", KeywordTrend.UP, null));
        entityManager.flush();
        entityManager.clear();

        List<PopularKeyword> keywords = popularKeywordRepository.findAllByOrderByRankAsc(PageRequest.of(0, 10));

        assertThat(keywords).extracting(PopularKeyword::getRank).containsExactly(2, 7);
        assertThat(keywords).extracting(PopularKeyword::getKeyword).containsExactly("편의점", "바리스타");
    }
}
