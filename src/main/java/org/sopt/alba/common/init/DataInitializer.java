package org.sopt.alba.common.init;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.alba.domain.job.entity.JobCategory;
import org.sopt.alba.domain.job.entity.JobPosting;
import org.sopt.alba.domain.job.entity.JobRequirement;
import org.sopt.alba.domain.job.entity.Region;
import org.sopt.alba.domain.job.enums.CareerRequirement;
import org.sopt.alba.domain.job.enums.EmploymentType;
import org.sopt.alba.domain.job.enums.ExposureType;
import org.sopt.alba.domain.job.enums.LanguageLevel;
import org.sopt.alba.domain.job.enums.PayType;
import org.sopt.alba.domain.job.enums.WorkPeriod;
import org.sopt.alba.domain.job.enums.Workday;
import org.sopt.alba.domain.job.repository.JobCategoryRepository;
import org.sopt.alba.domain.job.repository.JobPostingRepository;
import org.sopt.alba.domain.job.repository.RegionRepository;
import org.sopt.alba.domain.member.entity.Application;
import org.sopt.alba.domain.member.entity.Member;
import org.sopt.alba.domain.member.entity.Resume;
import org.sopt.alba.domain.member.enums.ResumeStatus;
import org.sopt.alba.domain.member.repository.ApplicationRepository;
import org.sopt.alba.domain.member.repository.MemberRepository;
import org.sopt.alba.domain.member.repository.ResumeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/*
 * 서버 시작 시 H2(in-memory)에 더미 데이터를 넣는다. (재시작하면 다시 들어감)
 * 순서: region >> job_category >> job_posting >> member >> resume >> application
 * TODO 기획 더미 시트가 확정되면 이 내용을 시트 데이터로 교체
 * TODO popular_keyword는 검색 도메인 구현 때 추가
 *
 * [이력서 매칭 확인용 회원]
 *  회원 1 (김알바)   : 작성 완료 이력서 3개 + 작성 중 1개, 지원 내역 1건
 *  회원 2 (이신입)   : 이력서 없음        → NO_RESUME
 *  회원 3 (박작성중) : 작성 중 이력서만   → DRAFT_ONLY
 */
@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Set<Workday> WEEKEND = EnumSet.of(Workday.SAT, Workday.SUN);
    private static final Set<Workday> WEEKDAYS =
            EnumSet.of(Workday.MON, Workday.TUE, Workday.WED, Workday.THU, Workday.FRI);

    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobPostingRepository jobPostingRepository;
    private final MemberRepository memberRepository;
    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;
    private final Clock clock;

    /* 시·도 ID → 시·군·구 목록 (시·군·구 ID = 시·도 ID * 100 + 순번) */
    private final Map<Long, List<Region>> sigunguBySido = new LinkedHashMap<>();
    private final Map<String, JobCategory> categories = new HashMap<>();

    private JobPosting appliedPosting;   // 회원 1이 이미 지원한 공고

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (regionRepository.count() > 0) {
            log.info("[DataInitializer] 이미 데이터가 있어 건너뜁니다.");
            return;
        }
        insertRegions();
        insertJobCategories();
        insertJobPostings();
        insertResumeMatchPostings();
        insertMembersAndResumes();
        log.info("[DataInitializer] 더미 데이터 삽입 완료 (지역 {}건, 공고 {}건, 회원 {}건, 이력서 {}건)",
                regionRepository.count(), jobPostingRepository.count(),
                memberRepository.count(), resumeRepository.count());
    }

    /* 메인 화면 지역 칩 순서와 같은 17개 시·도 */
    private void insertRegions() {
        saveSido(1L, "서울", "강남구", "마포구", "송파구", "종로구");
        saveSido(2L, "경기", "수원시", "성남시", "고양시");
        saveSido(3L, "인천", "남동구", "부평구", "연수구");
        saveSido(4L, "부산", "해운대구", "부산진구", "수영구");
        saveSido(5L, "대구", "중구", "수성구", "달서구");
        saveSido(6L, "광주", "서구", "북구", "광산구");
        saveSido(7L, "대전", "유성구", "서구", "중구");
        saveSido(8L, "울산", "남구", "중구", "북구");
        saveSido(9L, "세종", "세종시");
        saveSido(10L, "강원", "춘천시", "원주시", "강릉시");
        saveSido(11L, "충북", "청주시", "충주시");
        saveSido(12L, "충남", "천안시", "아산시");
        saveSido(13L, "전북", "전주시", "익산시");
        saveSido(14L, "전남", "여수시", "순천시");
        saveSido(15L, "경북", "포항시", "경주시");
        saveSido(16L, "경남", "창원시", "김해시");
        saveSido(17L, "제주", "제주시", "서귀포시");
    }

    private void saveSido(Long sidoId, String sidoName, String... sigunguNames) {
        Region sido = regionRepository.save(Region.sido(sidoId, sidoName));
        List<Region> children = new ArrayList<>();
        for (int i = 0; i < sigunguNames.length; i++) {
            children.add(regionRepository.save(Region.sigungu(sidoId * 100 + i + 1, sigunguNames[i], sido)));
        }
        sigunguBySido.put(sidoId, children);
    }

    private void insertJobCategories() {
        JobCategory food = saveCategory(JobCategory.main("A", "외식·음료"));
        JobCategory store = saveCategory(JobCategory.main("B", "유통·판매"));
        JobCategory service = saveCategory(JobCategory.main("C", "서비스"));

        saveCategory(JobCategory.sub("A01", "카페", food));
        saveCategory(JobCategory.sub("A02", "일반음식점", food));
        saveCategory(JobCategory.sub("A03", "베이커리", food));
        saveCategory(JobCategory.sub("B01", "편의점", store));
        saveCategory(JobCategory.sub("B02", "의류·잡화매장", store));
        saveCategory(JobCategory.sub("C01", "매장관리", service));
    }

    private JobCategory saveCategory(JobCategory category) {
        categories.put(category.getCode(), jobCategoryRepository.save(category));
        return category;
    }

    /*
     * 시·도마다 모집 중 5건 + 마감 1건. (지역별 추천 알바 확인용)
     *  - 모집 중이 5건이라 '4건만 내려오는지' 확인 가능
     *  - 마감 1건은 등록일이 가장 최신이라 '마감 공고가 빠지는지' 확인 가능
     * 예외: 세종(9)은 0건 → 빈 배열 응답, 제주(17)는 모집 중 2건 → 4건 미만 응답
     */
    private void insertJobPostings() {
        LocalDate today = LocalDate.now(clock);

        for (Map.Entry<Long, List<Region>> entry : sigunguBySido.entrySet()) {
            long sidoId = entry.getKey();
            List<Region> sigungus = entry.getValue();
            if (sidoId == 9L) {
                continue;
            }
            int openCount = (sidoId == 17L) ? 2 : 5;

            for (int i = 0; i < openCount; i++) {
                Template t = TEMPLATES[i];
                save(t, sigungus.get(i % sigungus.size()), today.minusDays(t.daysAgo), today.plusDays(14 + i));
            }
            // 마감 공고: 등록은 오늘이지만 마감일이 어제 → 목록에 나오면 안 됨
            save(CLOSED_TEMPLATE, sigungus.get(0), today, today.minusDays(1));
        }
    }

    /*
     * 이력서 매칭 확인용 서울 카페 공고. 등록일을 50일 전 이후로 둬서 지역별 추천(최신 4건)에는 영향이 없다.
     * 회원 1의 '카페 주말 알바 이력서' 기준 예상 점수 (기본점수 / 가점)
     *   스타벅스 강남구점(위 공통 공고) : 100 / 14  → 1위
     *   투썸플레이스                     :  90 /  0  → 2위
     *   메가커피                         :  80 /  0  → 3위 (이디야와 동점, 등록일이 더 최근)
     *   이디야                           :  80 /  0  → 4위라서 빠짐
     *   할리스                           :  55        → 기본점수 65점 미만이라 제외
     *   빽다방                           : 지원한 공고라 제외
     *   컴포즈커피                       : 전화 지원만 가능해서 제외
     */
    private void insertResumeMatchPostings() {
        LocalDate today = LocalDate.now(clock);
        List<Region> seoul = sigunguBySido.get(1L);
        JobCategory cafe = categories.get("A01");

        saveCafe("투썸플레이스 마포구점", "주 4일 카페 스태프", seoul.get(1), cafe, today.minusDays(50),
                EnumSet.of(Workday.THU, Workday.FRI, Workday.SAT, Workday.SUN), WorkPeriod.SIX_MONTHS_TO_ONE_YEAR,
                EmploymentType.PART_TIME, "온라인지원", JobRequirement.none());

        saveCafe("이디야 종로구점", "주말 카페 매니저", seoul.get(3), cafe, today.minusDays(70),
                WEEKEND, WorkPeriod.OVER_ONE_YEAR, EmploymentType.FULL_TIME, "온라인지원",
                JobRequirement.none());

        saveCafe("메가커피 송파구점", "주말 마감 바리스타", seoul.get(2), cafe, today.minusDays(60),
                WEEKEND, WorkPeriod.ONE_TO_THREE_MONTHS, EmploymentType.PART_TIME, "온라인지원",
                JobRequirement.builder().careerRequirement(CareerRequirement.REQUIRED).careerMonths(12).build());

        saveCafe("할리스 강남구점", "평일 오픈 단기 알바", seoul.get(0), cafe, today.minusDays(80),
                WEEKDAYS, WorkPeriod.ONE_DAY, EmploymentType.PART_TIME, "온라인지원",
                JobRequirement.builder().careerRequirement(CareerRequirement.REQUIRED).careerMonths(24).build());

        appliedPosting = saveCafe("빽다방 강남구점", "주말 오후 바리스타", seoul.get(0), cafe, today.minusDays(55),
                WEEKEND, WorkPeriod.THREE_TO_SIX_MONTHS, EmploymentType.PART_TIME, "온라인지원",
                JobRequirement.none());

        saveCafe("컴포즈커피 마포구점", "주말 바리스타 (전화 지원)", seoul.get(1), cafe, today.minusDays(65),
                WEEKEND, WorkPeriod.THREE_TO_SIX_MONTHS, EmploymentType.PART_TIME, "전화지원",
                JobRequirement.none());
    }

    private void insertMembersAndResumes() {
        LocalDateTime now = LocalDateTime.now(clock);

        Member kim = memberRepository.save(Member.create("김알바", null));       // id 1
        memberRepository.save(Member.create("이신입", null));                    // id 2: 이력서 없음
        Member park = memberRepository.save(Member.create("박작성중", null));    // id 3: 작성 중만

        // 이력서 1: 메인 화면의 기준 이력서 (작성 완료 중 가장 최근 수정)
        resumeRepository.save(Resume.builder()
                .member(kim).title("카페 주말 알바 이력서").status(ResumeStatus.COMPLETED)
                .desiredRegionIds(List.of(1L))                       // 서울 전체
                .desiredCategoryCodes(List.of("A01"))                // 카페
                .desiredWorkdays(WEEKEND)
                .desiredWorkPeriods(EnumSet.of(WorkPeriod.THREE_TO_SIX_MONTHS, WorkPeriod.SIX_MONTHS_TO_ONE_YEAR))
                .desiredEmploymentTypes(EnumSet.of(EmploymentType.PART_TIME))
                .careerCategoryCode("A01").careerMonths(8)
                .skills(List.of("에스프레소 추출", "POS 사용"))
                .certificates(List.of("바리스타 2급"))
                .language("영어").languageLevel(LanguageLevel.INTERMEDIATE)
                .computerSkill(true)
                .modifiedAt(now.minusDays(1))
                .build());

        // 이력서 2: 이력서를 바꾸면 추천이 달라지는지 확인용 (부산, 유통·판매 대분류, 요일·기간 무관)
        resumeRepository.save(Resume.builder()
                .member(kim).title("편의점·판매 이력서").status(ResumeStatus.COMPLETED)
                .desiredRegionIds(List.of(4L))                       // 부산 전체
                .desiredCategoryCodes(List.of("B"))                  // 유통·판매 (하위 편의점, 의류·잡화매장 포함)
                .workdaysAny(true)
                .workPeriodAny(true)
                .desiredEmploymentTypes(EnumSet.of(EmploymentType.PART_TIME))
                .modifiedAt(now.minusDays(5))
                .build());

        // 이력서 3: 맞는 공고가 없는 이력서 (세종에는 공고가 없음) → NO_RECOMMENDATION
        resumeRepository.save(Resume.builder()
                .member(kim).title("세종 매장관리 이력서").status(ResumeStatus.COMPLETED)
                .desiredRegionIds(List.of(9L))
                .desiredCategoryCodes(List.of("C01"))
                .workdaysAny(true)
                .workPeriodAny(true)
                .desiredEmploymentTypes(EnumSet.of(EmploymentType.PART_TIME))
                .modifiedAt(now.minusDays(10))
                .build());

        // 이력서 4: 작성 중 → 드롭다운에는 보이지만 기준 이력서로 고르면 400
        resumeRepository.save(Resume.builder()
                .member(kim).title("작성 중인 이력서").status(ResumeStatus.DRAFT)
                .modifiedAt(now)
                .build());

        // 이력서 5: 회원 3의 작성 중 이력서
        resumeRepository.save(Resume.builder()
                .member(park).title("처음 쓰는 이력서").status(ResumeStatus.DRAFT)
                .modifiedAt(now.minusDays(2))
                .build());

        applicationRepository.save(Application.create(kim, appliedPosting, now.minusDays(3)));
    }

    private void save(Template t, Region sigungu, LocalDate publishedAt, LocalDate deadline) {
        jobPostingRepository.save(JobPosting.builder()
                .region(sigungu)
                .jobCategory(categories.get(t.categoryCode))
                .companyName(t.companyName + " " + sigungu.getName() + "점")
                .title(t.title)
                .imageUrl(null)                 // TODO 이미지 저장소 URL
                .applyMethod("온라인지원")
                .payType(t.payType)
                .payAmount(t.payAmount)
                .workPeriod(t.workPeriod)
                .employmentType(t.employmentType)
                .startTime(t.startTime)
                .endTime(t.endTime)
                .deadline(t.alwaysOpen ? null : deadline)
                .exposureType(t.exposureType)
                .publishedAt(publishedAt)
                .workdays(t.workdays)
                .requirement(t.requirement)
                .build());
    }

    private JobPosting saveCafe(String companyName, String title, Region sigungu, JobCategory category,
                                LocalDate publishedAt, Set<Workday> workdays, WorkPeriod workPeriod,
                                EmploymentType employmentType, String applyMethod, JobRequirement requirement) {
        return jobPostingRepository.save(JobPosting.builder()
                .region(sigungu)
                .jobCategory(category)
                .companyName(companyName)
                .title(title)
                .imageUrl(null)                 // TODO 이미지 저장소 URL
                .applyMethod(applyMethod)
                .payType(PayType.HOURLY)
                .payAmount(10500)
                .workPeriod(workPeriod)
                .employmentType(employmentType)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(15, 0))
                .deadline(null)
                .exposureType(ExposureType.NORMAL)
                .publishedAt(publishedAt)
                .workdays(workdays)
                .requirement(requirement)
                .build());
    }

    private record Template(String companyName, String title, String categoryCode, PayType payType, int payAmount,
                            WorkPeriod workPeriod, EmploymentType employmentType, LocalTime startTime,
                            LocalTime endTime, Set<Workday> workdays, ExposureType exposureType, int daysAgo,
                            boolean alwaysOpen, JobRequirement requirement) {
    }

    /* daysAgo: 0 = 오늘, 2 = 2일 전, 7 = 7일 전, 40 = 1달 전, 400 = 1년 전 → 등록일 라벨 종류를 모두 확인 */
    private static final Template[] TEMPLATES = {
            new Template("스타벅스", "주말 오전 바리스타 모집", "A01", PayType.HOURLY, 10500,
                    WorkPeriod.THREE_TO_SIX_MONTHS, EmploymentType.PART_TIME,
                    LocalTime.of(7, 0), LocalTime.of(13, 0), WEEKEND, ExposureType.PREMIUM, 0, false,
                    JobRequirement.builder()
                            .careerRequirement(CareerRequirement.PREFERRED).careerMonths(6)
                            .preferredSkills(List.of("에스프레소 추출", "POS 사용", "라떼아트"))
                            .preferredCertificates(List.of("바리스타 2급"))
                            .preferredLanguage("영어").preferredLanguageLevel(LanguageLevel.BASIC)
                            .computerSkillPreferred(true)
                            .build()),
            new Template("CU", "평일 야간 편의점 스태프", "B01", PayType.HOURLY, 10030,
                    WorkPeriod.SIX_MONTHS_TO_ONE_YEAR, EmploymentType.PART_TIME,
                    LocalTime.of(22, 0), LocalTime.of(6, 0), WEEKDAYS, ExposureType.NORMAL, 2, false,
                    JobRequirement.none()),
            new Template("파리바게뜨", "오픈 판매 직원 구합니다", "A03", PayType.MONTHLY, 2300000,
                    WorkPeriod.OVER_ONE_YEAR, EmploymentType.FULL_TIME,
                    LocalTime.of(6, 30), LocalTime.of(15, 30), WEEKDAYS, ExposureType.LIGHT, 7, true,
                    JobRequirement.builder().careerRequirement(CareerRequirement.REQUIRED).careerMonths(12).build()),
            new Template("ABC마트", "매장 판매·재고 관리", "B02", PayType.HOURLY, 11000,
                    WorkPeriod.ONE_TO_THREE_MONTHS, EmploymentType.PART_TIME,
                    LocalTime.of(13, 0), LocalTime.of(22, 0), EnumSet.of(Workday.FRI, Workday.SAT, Workday.SUN),
                    ExposureType.NORMAL, 40, false,
                    JobRequirement.builder().careerRequirement(CareerRequirement.ENTRY_OK).build()),
            new Template("아웃백스테이크하우스", "홀 서빙 주말 파트타이머", "A02", PayType.DAILY, 110000,
                    WorkPeriod.ONE_WEEK_TO_ONE_MONTH, EmploymentType.PART_TIME,
                    LocalTime.of(11, 0), LocalTime.of(21, 0), WEEKEND, ExposureType.NORMAL, 400, false,
                    JobRequirement.none()),
    };

    private static final Template CLOSED_TEMPLATE =
            new Template("다이소", "[마감] 매장 관리 스태프", "C01", PayType.HOURLY, 10030,
                    WorkPeriod.THREE_TO_SIX_MONTHS, EmploymentType.PART_TIME,
                    LocalTime.of(10, 0), LocalTime.of(19, 0),
                    EnumSet.of(Workday.MON, Workday.WED, Workday.FRI), ExposureType.NORMAL, 0, false,
                    JobRequirement.none());
}
            new Template("다이소", "[마감] 매장 관리 스태프", 5, PayType.HOURLY, 10030, "3개월~6개월",
                    LocalTime.of(10, 0), LocalTime.of(19, 0), Set.of(Workday.MON, Workday.WED, Workday.FRI),
                    ExposureType.NORMAL, 0, false);
/**
 * 서버 시작 시 H2(in-memory)에 더미 데이터를 넣는다. (재시작하면 다시 들어감)
 * 순서: region >> job_category >> job_posting(+workday) >> member >> resume >> application >> popular_keyword
 * 기획 시트가 확정되면 각 insertXxx()를 채운다. (CSV/JSON 파일을 resources/seed/ 에 두고 읽는 방식 권장)
 */

@Component
@Profile({"local", "dev"})
public class DataInitializer implements ApplicationRunner {

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // TODO Repository를 연결할 때 기존 데이터가 있으면 초기화를 건너뛴다.
        insertRegions();
        insertJobCategories();
        insertJobPostings();
        insertMember();
        insertResumes();
        insertApplications();
        insertPopularKeywords();
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
