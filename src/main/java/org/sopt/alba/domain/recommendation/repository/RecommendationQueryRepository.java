package org.sopt.alba.domain.recommendation.repository;

import static org.sopt.alba.domain.job.entity.QJobCategory.jobCategory;
import static org.sopt.alba.domain.job.entity.QJobPosting.jobPosting;
import static org.sopt.alba.domain.job.entity.QRegion.region;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.sopt.alba.domain.job.entity.JobPosting;
import org.sopt.alba.domain.job.entity.QJobCategory;
import org.sopt.alba.domain.job.entity.QRegion;
import org.sopt.alba.domain.job.enums.Workday;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecommendationQueryRepository {

    private static final QRegion sido = new QRegion("sido");

    private final JPAQueryFactory queryFactory;
    private static final QJobCategory mainCategory = new QJobCategory("mainCategory");

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

    // TODO 공고 검색 API의 조건 메서드가 만들어지면 키워드, categoryCode, workdays 조건은 그쪽 걸로
    public List<JobPosting> findMatchCandidates(LocalDate today, Collection<Long> appliedJobIds,
                                                Collection<Long> regionIds, String keyword,
                                                Collection<String> categoryCodes, Collection<Workday> workdays) {
        return queryFactory
                .selectFrom(jobPosting)
                .join(jobPosting.region, region).fetchJoin()
                .leftJoin(region.parent, sido).fetchJoin()
                .join(jobPosting.jobCategory, jobCategory).fetchJoin()
                .leftJoin(jobCategory.parent, mainCategory).fetchJoin()
                .where(
                        isOpen(today),
                        notApplied(appliedJobIds),
                        inRegions(regionIds),
                        containsKeyword(keyword),
                        inCategories(categoryCodes),
                        hasAnyWorkday(workdays)
                )
                .fetch();
    }

    /* 업직종 코드- 상위 코드*/
    public Map<String, String> findCategoryParentCodes() {
        List<Tuple> rows = queryFactory
                .select(jobCategory.code, mainCategory.code)
                .from(jobCategory)
                .leftJoin(jobCategory.parent, mainCategory)
                .fetch();

        Map<String, String> parentCodes = new HashMap<>();
        for (Tuple row : rows) {
            parentCodes.put(row.get(jobCategory.code), row.get(mainCategory.code));
        }
        return parentCodes;
    }

    /* 이름에 검색어가 들어간 업직종 코드 */
    // TODO 얘도 스프레드 시트 들어오면 다시 확인하기
    public List<String> findCategoryCodesByKeyword(String keyword) {
        return queryFactory
                .select(jobCategory.code)
                .from(jobCategory)
                .where(jobCategory.name.containsIgnoreCase(keyword))
                .fetch();
    }

    /* 마감일이 없거나 오늘 이후인 공고 */
    private BooleanExpression isOpen(LocalDate today) {
        return jobPosting.deadline.isNull()
                .or(jobPosting.deadline.goe(today));
    }

    private BooleanExpression notApplied(Collection<Long> appliedJobIds) {
        return isEmpty(appliedJobIds) ? null : jobPosting.id.notIn(appliedJobIds);
    }

    private BooleanExpression inRegions(Collection<Long> regionIds) {
        return isEmpty(regionIds) ? null : region.id.in(regionIds).or(sido.id.in(regionIds));
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        return jobPosting.title.containsIgnoreCase(keyword)
                .or(jobPosting.companyName.containsIgnoreCase(keyword))
                .or(jobCategory.name.containsIgnoreCase(keyword));
    }

    private BooleanExpression inCategories(Collection<String> categoryCodes) {
        return isEmpty(categoryCodes)
                ? null
                : jobCategory.code.in(categoryCodes).or(mainCategory.code.in(categoryCodes));
    }

    private BooleanExpression hasAnyWorkday(Collection<Workday> workdays) {
        return isEmpty(workdays) ? null : jobPosting.workdays.any().in(workdays);
    }

    private boolean isEmpty(Collection<?> values) {
        return values == null || values.isEmpty();
    }
}