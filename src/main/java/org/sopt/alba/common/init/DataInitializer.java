package org.sopt.alba.common.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.alba.domain.job.repository.JobCategoryRepository;
import org.sopt.alba.domain.job.repository.JobPostingRepository;
import org.sopt.alba.domain.job.repository.RegionRepository;
import org.sopt.alba.domain.member.repository.ApplicationRepository;
import org.sopt.alba.domain.member.repository.MemberRepository;
import org.sopt.alba.domain.member.repository.ResumeRepository;
import org.sopt.alba.domain.search.repository.PopularKeywordRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 시작 시 H2(in-memory)에 더미 데이터를 넣는다. (재시작하면 다시 들어감)
 * 순서: region >> job_category >> job_posting(+workday) >> member >> resume >> application >> popular_keyword
 * 기획 시트가 확정되면 각 insertXxx()를 채운다. (CSV/JSON 파일을 resources/seed/ 에 두고 읽는 방식 권장)
 */

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobPostingRepository jobPostingRepository;
    private final MemberRepository memberRepository;
    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;
    private final PopularKeywordRepository popularKeywordRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (memberRepository.count() > 0) {
            log.info("[DataInitializer] 이미 데이터가 있어 건너뜁니다.");
            return;
        }
        insertRegions();
        insertJobCategories();
        insertJobPostings();
        insertMember();
        insertResumes();
        insertApplications();
        insertPopularKeywords();
        log.info("[DataInitializer] 더미 데이터 삽입 완료");
    }

    private void insertRegions() {
        // TODO Region.sido(...) 저장 후 Region.sigungu(..., sido) 저장
    }

    private void insertJobCategories() {
        // TODO JobCategory.main(...) 저장 후 JobCategory.sub(..., main) 저장
    }

    private void insertJobPostings() {
        // TODO JobPosting.builder()...workdays(Set.of(Workday.SAT, Workday.SUN)).build()
    }

    private void insertMember() {
        // TODO Member.create("김알바", null) — 목업 회원 1명 (id = 1)
    }

    private void insertResumes() {
        // TODO Resume.builder()...status(COMPLETED / DRAFT)...build()
    }

    private void insertApplications() {
        // TODO Application.create(member, jobPosting, appliedAt)
    }

    private void insertPopularKeywords() {
        // TODO PopularKeyword.create(1, "카페", KeywordTrend.UP, "A01") ... 10개
    }
}