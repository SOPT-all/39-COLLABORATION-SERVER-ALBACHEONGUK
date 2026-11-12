package org.sopt.alba.domain.search.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.alba.domain.search.enums.KeywordTrend;

@Entity
@Table(name = "popular_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PopularKeyword {

    @Id
    private Integer rank;

    @Column(nullable = false, unique = true, length = 100)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private KeywordTrend trend;

    @Column(length = 20)
    private String categoryCode;
}
