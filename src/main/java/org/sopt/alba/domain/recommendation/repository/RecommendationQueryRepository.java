package org.sopt.alba.domain.recommendation.repository;

import static org.sopt.alba.domain.job.entity.QJobPosting.jobPosting;
import static org.sopt.alba.domain.job.entity.QRegion.region;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.domain.job.entity.JobPosting;
import org.sopt.alba.domain.job.entity.QRegion;
import org.springframework.stereotype.Repository;

/*추천 기능 전용 조회 쿼리. job 도메인의 Repository를 수정하지 않고 QueryDSL Q클래스를 사용하도록 구현함*/

@Repository
@RequiredArgsConstructor
public class RecommendationQueryRepository {

    private static final QRegion sido = new QRegion("sido");

    private final JPAQueryFactory queryFactory;

    /* 시/도 아래 시/군/구의 마감되지 않은 공고를 조회한다.
     정렬은 최신 등록순, ID 오름차순*/

    public List<JobPosting> findOpenBySido(Long sidoId, LocalDate today, int limit) {
        return queryFactory
                .selectFrom(jobPosting)
                .join(jobPosting.region, region).fetchJoin()
                .join(region.parent, sido).fetchJoin()
                .join(jobPosting.jobCategory).fetchJoin()
                .where(
                        sido.id.eq(sidoId),
                        isOpen(today)
                )
                .orderBy(jobPosting.publishedAt.desc(), jobPosting.id.asc())
                .limit(limit)
                .fetch();
    }

    /* 마감일이 없거나 오늘 이후인 공고 */
    private BooleanExpression isOpen(LocalDate today) {
        return jobPosting.deadline.isNull()
                .or(jobPosting.deadline.goe(today));
    }
}