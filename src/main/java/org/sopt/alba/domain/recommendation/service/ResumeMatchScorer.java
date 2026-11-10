package org.sopt.alba.domain.recommendation.service;

import java.util.List;
import java.util.Set;
import org.sopt.alba.domain.job.entity.JobCategory;
import org.sopt.alba.domain.job.entity.JobPosting;
import org.sopt.alba.domain.job.entity.JobRequirement;
import org.sopt.alba.domain.job.enums.EmploymentType;
import org.sopt.alba.domain.job.enums.WorkPeriod;
import org.sopt.alba.domain.job.enums.Workday;
import org.sopt.alba.domain.member.entity.Resume;
import org.springframework.stereotype.Component;

/*
기본점수 100점 = 희망 업직종 40 + 희망 근무요일 20 + 희망 근무기간 10 + 희망 근무형태 15 + 관련 경력 15
가점 최대 15점 = 업무 스킬(+3씩 최대 2개) + 자격증 +3 + 외국어 +3 + 컴퓨터 활용 +2 + 포트폴리오 +1
우대조건은 제외 기준이 아님.
경력은 기본점수, 나머지 우대사항은 가점으로만 반영
 */

@Component
public class ResumeMatchScorer {

    public static final int MIN_BASE_SCORE = 65;      // 기본점수가 이거보다 작으면 추천에서 제외

    private static final int CATEGORY_MATCH = 40;

    private static final int WORKDAYS_FULL = 20;
    private static final int WORKDAYS_PARTIAL = 10;

    private static final int PERIOD_FULL = 10;
    private static final int PERIOD_PARTIAL = 5;

    private static final int EMPLOYMENT_MATCH = 15;

    private static final int CAREER_FULL = 15;
    private static final int CAREER_PARTIAL = 10;

    private static final int SKILL_BONUS_EACH = 3;
    private static final int SKILL_BONUS_MAX_COUNT = 2;
    private static final int CERTIFICATE_BONUS = 3;
    private static final int LANGUAGE_BONUS = 3;
    private static final int COMPUTER_BONUS = 2;
    private static final int PORTFOLIO_BONUS = 1;
    private static final int MAX_BONUS = 15;

    public MatchScore score(Resume resume, JobPosting posting) {
        int base = categoryScore(resume, posting)
                + workdayScore(resume, posting)
                + workPeriodScore(resume, posting)
                + employmentScore(resume, posting)
                + careerScore(resume, posting);
        return new MatchScore(base, bonusScore(resume, posting));
    }

    /* 공고 업직종이 희망 업직종과 같거나 그 하위이면 40, 아니면 0 */
    private int categoryScore(Resume resume, JobPosting posting) {
        return isSameOrUnder(posting.getJobCategory(), resume.getDesiredCategoryCodes()) ? CATEGORY_MATCH : 0;
    }

    /* 요일무관이거나 공고 요일이 모두 희망 요일에 포함되면 20, 일부만 겹치면 10, 안 겹치면 0 */
    private int workdayScore(Resume resume, JobPosting posting) {
        Set<Workday> postingDays = posting.getWorkdays();
        if (resume.isWorkdaysAny()) {
            return WORKDAYS_FULL;
        }
        if (postingDays.isEmpty()) {
            return WORKDAYS_FULL;
        }
        Set<Workday> desired = resume.getDesiredWorkdaySet();
        long matched = postingDays.stream().filter(desired::contains).count();
        if (matched == postingDays.size()) {
            return WORKDAYS_FULL;
        }
        return matched > 0 ? WORKDAYS_PARTIAL : 0;
    }

    /* 기간무관이거나 공고 기간이 희망 기간 안이면 10, 일부면 5, 아니면 0 */
    private int workPeriodScore(Resume resume, JobPosting posting) {
        if (resume.isWorkPeriodAny()) {
            return PERIOD_FULL;
        }
        WorkPeriod postingPeriod = posting.getWorkPeriod();
        if (postingPeriod == null) {
            return 0;
        }
        Set<WorkPeriod> desired = resume.getDesiredWorkPeriodSet();
        if (desired.contains(postingPeriod)) {
            return PERIOD_FULL;
        }
        boolean adjacent = desired.stream().anyMatch(postingPeriod::isAdjacentTo);
        return adjacent ? PERIOD_PARTIAL : 0;
    }

    /* 희망 근무 - 일치 15, 불일치 0 */
    private int employmentScore(Resume resume, JobPosting posting) {
        Set<EmploymentType> desired = resume.getDesiredEmploymentTypeSet();
        return desired.contains(posting.getEmploymentType()) ? EMPLOYMENT_MATCH : 0;
    }

    /* 신입 가능, 경력무관: 15, 필수 경력: 기간 충족 15, 아니면 0
     * 우대 경력: 기간 충족 15, 관련 경력은 있지만 기간 미달 10, 관련 경력 없음 0*/

    private int careerScore(Resume resume, JobPosting posting) {
        JobRequirement requirement = posting.getRequirement();
        int relatedMonths = relatedCareerMonths(resume, posting);

        return switch (requirement.getCareerRequirement()) {
            case ENTRY_OK, ANY -> CAREER_FULL;
            case REQUIRED -> relatedMonths >= requirement.getCareerMonths() && relatedMonths > 0 ? CAREER_FULL : 0;
            case PREFERRED -> {
                if (relatedMonths <= 0) {
                    yield 0;
                }
                yield relatedMonths >= requirement.getCareerMonths() ? CAREER_FULL : CAREER_PARTIAL;
            }
        };
    }

    /* 이력서 경력의 업직종이 공고 업직종과 같거나 공고의 상위 업직종이면 관련 경력으로 본다. */
    // TODO '관련 경력'의 범위 미정 (같은 대분류의 다른 소분류 경력도 인정할지)
    private int relatedCareerMonths(Resume resume, JobPosting posting) {
        String careerCode = resume.getCareerCategoryCode();
        if (careerCode == null) {
            return 0;
        }
        return isSameOrUnder(posting.getJobCategory(), List.of(careerCode)) ? resume.getCareerMonths() : 0;
    }

    /* 가점 (최대 15점) */
    private int bonusScore(Resume resume, JobPosting posting) {
        JobRequirement requirement = posting.getRequirement();
        int bonus = 0;

        // 업무 스킬: 우대 스킬 1개 일치당 +3 (최대 2개)
        long matchedSkills = requirement.getPreferredSkills().stream()
                .filter(resume.getSkills()::contains)
                .count();
        bonus += (int) Math.min(matchedSkills, SKILL_BONUS_MAX_COUNT) * SKILL_BONUS_EACH;

        // 자격증 +3: 우대 자격증 중 하나라도 보유
        boolean hasCertificate = requirement.getPreferredCertificates().stream()
                .anyMatch(resume.getCertificates()::contains);
        if (hasCertificate) {
            bonus += CERTIFICATE_BONUS;
        }

        // 외국어 +3: 같은 언어이고 요구 수준 이상
        if (requirement.getPreferredLanguage() != null
                && requirement.getPreferredLanguage().equals(resume.getLanguage())
                && resume.getLanguageLevel() != null
                && (requirement.getPreferredLanguageLevel() == null
                || resume.getLanguageLevel().isAtLeast(requirement.getPreferredLanguageLevel()))) {
            bonus += LANGUAGE_BONUS;
        }

        // 컴퓨터 활용 +2
        if (requirement.isComputerSkillPreferred() && resume.isComputerSkill()) {
            bonus += COMPUTER_BONUS;
        }

        // 포트폴리오 +1: 공고에 우대 명시 + 포트폴리오 분야가 공고 업직종과 일치
        if (requirement.isPortfolioPreferred()
                && resume.getPortfolioCategoryCode() != null
                && isSameOrUnder(posting.getJobCategory(), List.of(resume.getPortfolioCategoryCode()))) {
            bonus += PORTFOLIO_BONUS;
        }

        return Math.min(bonus, MAX_BONUS);
    }

    /* 공고 업직종(소분류)이 codes 중 하나와 같거나, 그 상위(대분류)가 codes에 들어 있는지 */
    private boolean isSameOrUnder(JobCategory postingCategory, List<String> codes) {
        if (codes.contains(postingCategory.getCode())) {
            return true;
        }
        JobCategory parent = postingCategory.getParent();
        return parent != null && codes.contains(parent.getCode());
    }

    public record MatchScore(int base, int bonus) {

        public int total() {
            return base + bonus;
        }

        public boolean isRecommendable() {
            return base >= MIN_BASE_SCORE;
        }
    }
}