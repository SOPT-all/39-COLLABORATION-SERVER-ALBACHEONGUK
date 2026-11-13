package org.sopt.alba.domain.job.repository;

import static org.sopt.alba.domain.job.entity.QJobPosting.jobPosting;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.sopt.alba.domain.job.dto.request.JobSearchRequest;
import org.sopt.alba.domain.job.dto.request.JobSearchRequest.SortType;
import org.sopt.alba.domain.job.entity.JobPosting;
import org.sopt.alba.domain.job.entity.QJobCategory;
import org.sopt.alba.domain.job.entity.QRegion;
import org.sopt.alba.domain.job.enums.ExposureType;
import org.sopt.alba.domain.job.enums.PayType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class JobSearchRepository {

    private static final QRegion DISTRICT = new QRegion("district");
    private static final QRegion PROVINCE = new QRegion("province");
    private static final QJobCategory CATEGORY = new QJobCategory("category");
    private static final QJobCategory MAIN_CATEGORY = new QJobCategory("mainCategory");
    private static final String JAVA_SPECIAL_CHARACTERS = "[^\\p{L}\\p{N}\\s]";
    private static final String POSTGRES_SPECIAL_CHARACTERS = "[^[:alnum:][:space:]]";

    private final JPAQueryFactory queryFactory;
    private final boolean isPostgreSql;

    public JobSearchRepository(JPAQueryFactory queryFactory, EntityManagerFactory entityManagerFactory) {
        this.queryFactory = queryFactory;
        this.isPostgreSql = entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getJdbcServices().getDialect() instanceof PostgreSQLDialect;
    }

    public Page<JobPosting> findJobs(JobSearchRequest request, String keyword, PayType payType, LocalDate today) {
        PageRequest page = PageRequest.of(request.page() - 1, request.size());
        BooleanBuilder filters = buildFilters(request, keyword, payType, today);

        Long count = matchingJobs(filters).select(jobPosting.count()).fetchOne();
        long totalCount = count == null ? 0 : count;
        if (page.getOffset() >= totalCount) {
            return new PageImpl<>(List.of(), page, totalCount);
        }

        List<Long> ids = matchingJobs(filters)
                .select(jobPosting.id)
                .orderBy(orderBySection())
                .orderBy(orderWithinSection(request.sort(), keyword))
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetch();

        return new PageImpl<>(findPostingsInOrder(ids), page, totalCount);
    }

    private JPAQuery<?> matchingJobs(BooleanBuilder filters) {
        return queryFactory.from(jobPosting)
                .join(jobPosting.region, DISTRICT)
                .leftJoin(DISTRICT.parent, PROVINCE)
                .join(jobPosting.jobCategory, CATEGORY)
                .leftJoin(CATEGORY.parent, MAIN_CATEGORY)
                .where(filters);
    }

    private BooleanBuilder buildFilters(JobSearchRequest request, String keyword, PayType payType, LocalDate today) {
        BooleanBuilder filters = new BooleanBuilder();
        if (!keyword.isEmpty()) {
            filters.and(normalized(jobPosting.title).contains(keyword)
                    .or(normalized(jobPosting.companyName).contains(keyword))
                    .or(normalized(CATEGORY.name).contains(keyword)));
        }
        if (!request.regionIds().isEmpty()) {
            filters.and(DISTRICT.id.in(request.regionIds()).or(PROVINCE.id.in(request.regionIds())));
        }
        if (!request.categoryCodes().isEmpty()) {
            filters.and(CATEGORY.code.in(request.categoryCodes()).or(MAIN_CATEGORY.code.in(request.categoryCodes())));
        }
        if (!request.workdays().isEmpty()) {
            filters.and(jobPosting.daysNegotiable.isFalse()
                    .and(jobPosting.workdays.any().in(request.workdays())));
        }
        if (payType != null) {
            filters.and(jobPosting.payType.eq(payType));
        }
        if (request.minPay() != null || request.maxPay() != null) {
            filters.and(jobPosting.payAmount.gt(0));
        }
        if (request.minPay() != null) {
            filters.and(jobPosting.payAmount.goe(request.minPay()));
        }
        if (request.maxPay() != null) {
            filters.and(jobPosting.payAmount.loe(request.maxPay()));
        }
        if ("true".equals(request.ongoing())) {
            filters.and(jobPosting.deadline.isNull().or(jobPosting.deadline.goe(today)));
        }
        return filters;
    }

    private OrderSpecifier<Integer> orderBySection() {
        return new CaseBuilder()
                .when(jobPosting.exposureType.eq(ExposureType.PREMIUM)).then(0)
                .when(jobPosting.exposureType.eq(ExposureType.LIGHT)).then(1)
                .otherwise(2)
                .asc();
    }

    private OrderSpecifier<?>[] orderWithinSection(SortType sort, String keyword) {
        if (sort == SortType.PUBLISHED_DESC || keyword.isEmpty()) {
            return new OrderSpecifier<?>[]{jobPosting.publishedAt.desc(), jobPosting.id.asc()};
        }
        StringExpression title = normalized(jobPosting.title);
        StringExpression company = normalized(jobPosting.companyName);
        StringExpression category = normalized(CATEGORY.name);
        OrderSpecifier<Integer> accuracy = new CaseBuilder()
                .when(title.eq(keyword)).then(6)
                .when(title.contains(keyword)).then(5)
                .when(company.eq(keyword)).then(4)
                .when(company.contains(keyword)).then(3)
                .when(category.eq(keyword)).then(2)
                .when(category.contains(keyword)).then(1)
                .otherwise(0)
                .desc();
        return new OrderSpecifier<?>[]{accuracy, jobPosting.publishedAt.desc(), jobPosting.id.asc()};
    }

    private StringExpression normalized(StringExpression field) {
        if (isPostgreSql) {
            return Expressions.stringTemplate("cast(function('regexp_replace', lower({0}), {1}, '', 'g') as string)",
                    field, POSTGRES_SPECIAL_CHARACTERS);
        }
        return Expressions.stringTemplate("cast(function('regexp_replace', lower({0}), {1}, '') as string)",
                field, JAVA_SPECIAL_CHARACTERS);
    }

    private List<JobPosting> findPostingsInOrder(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        // 요일은 공고마다 여러 행이므로 ID 기준으로 페이징한 뒤 상세를 가져온다.
        Map<Long, JobPosting> postings = queryFactory.selectFrom(jobPosting)
                .distinct()
                .join(jobPosting.region, DISTRICT).fetchJoin()
                .leftJoin(DISTRICT.parent, PROVINCE).fetchJoin()
                .join(jobPosting.jobCategory, CATEGORY).fetchJoin()
                .leftJoin(jobPosting.workdays).fetchJoin()
                .where(jobPosting.id.in(ids))
                .fetch().stream()
                .collect(Collectors.toMap(JobPosting::getId, Function.identity()));

        return ids.stream().filter(postings::containsKey).map(postings::get).toList();
    }
}
