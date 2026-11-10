package org.sopt.alba.common.init;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.alba.domain.job.entity.JobCategory;
import org.sopt.alba.domain.job.entity.JobPosting;
import org.sopt.alba.domain.job.entity.Region;
import org.sopt.alba.domain.job.enums.ExposureType;
import org.sopt.alba.domain.job.enums.PayType;
import org.sopt.alba.domain.job.enums.Workday;
import org.sopt.alba.domain.job.repository.JobCategoryRepository;
import org.sopt.alba.domain.job.repository.JobPostingRepository;
import org.sopt.alba.domain.job.repository.RegionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/*
 * 서버 시작 시 H2(in-memory)에 더미 데이터를 넣는다. (재시작하면 다시 들어감)
 * 지금은 '지역별 추천 알바'를 확인할 수 있는 임시 더미만 추가해봤어요: region >> job_category >> job_posting
 * TODO 기획 더미 시트가 확정되면 이 내용을 시트 데이터로 교체
 * TODO member / resume / application / popular_keyword는 각 도메인 구현 때 추가
 */

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobPostingRepository jobPostingRepository;
    private final Clock clock;

    private final Map<Long, List<Region>> sigunguBySido = new LinkedHashMap<>();
    private final List<JobCategory> subCategories = new ArrayList<>();

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
        log.info("[DataInitializer] 더미 데이터 삽입 완료 (지역 {}건, 공고 {}건)",
                regionRepository.count(), jobPostingRepository.count());
    }

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
        JobCategory food = jobCategoryRepository.save(JobCategory.main("A", "외식·음료"));
        JobCategory store = jobCategoryRepository.save(JobCategory.main("B", "유통·판매"));
        JobCategory service = jobCategoryRepository.save(JobCategory.main("C", "서비스"));

        subCategories.add(jobCategoryRepository.save(JobCategory.sub("A01", "카페", food)));
        subCategories.add(jobCategoryRepository.save(JobCategory.sub("A02", "일반음식점", food)));
        subCategories.add(jobCategoryRepository.save(JobCategory.sub("A03", "베이커리", food)));
        subCategories.add(jobCategoryRepository.save(JobCategory.sub("B01", "편의점", store)));
        subCategories.add(jobCategoryRepository.save(JobCategory.sub("B02", "의류·잡화매장", store)));
        subCategories.add(jobCategoryRepository.save(JobCategory.sub("C01", "매장관리", service)));
    }

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
            save(CLOSED_TEMPLATE, sigungus.get(0), today, today.minusDays(1));
        }
    }

    private void save(Template t, Region sigungu, LocalDate publishedAt, LocalDate deadline) {
        jobPostingRepository.save(JobPosting.builder()
                .region(sigungu)
                .jobCategory(subCategories.get(t.categoryIndex))
                .companyName(t.companyName + " " + sigungu.getName() + "점")
                .title(t.title)
                .dong(null)
                .nearbySubway(null)
                .imageUrl(null)
                .applyMethod("온라인지원")
                .payType(t.payType)
                .payAmount(t.payAmount)
                .workPeriod(t.workPeriod)
                .startTime(t.startTime)
                .endTime(t.endTime)
                .daysNegotiable(false)
                .timeNegotiable(false)
                .deadline(t.alwaysOpen ? null : deadline)
                .exposureType(t.exposureType)
                .publishedAt(publishedAt)
                .workdays(t.workdays)
                .build());
    }

    private record Template(String companyName, String title, int categoryIndex, PayType payType, int payAmount,
                            String workPeriod, LocalTime startTime, LocalTime endTime, Set<Workday> workdays,
                            ExposureType exposureType, int daysAgo, boolean alwaysOpen) {
    }

    private static final Template[] TEMPLATES = {
            new Template("스타벅스", "주말 오전 바리스타 모집", 0, PayType.HOURLY, 10500, "3개월~6개월",
                    LocalTime.of(7, 0), LocalTime.of(13, 0), Set.of(Workday.SAT, Workday.SUN),
                    ExposureType.PREMIUM, 0, false),
            new Template("CU", "평일 야간 편의점 스태프", 3, PayType.HOURLY, 10030, "6개월~1년",
                    LocalTime.of(22, 0), LocalTime.of(6, 0),
                    Set.of(Workday.MON, Workday.TUE, Workday.WED, Workday.THU, Workday.FRI),
                    ExposureType.NORMAL, 2, false),
            new Template("파리바게뜨", "오픈 판매 직원 구합니다", 2, PayType.MONTHLY, 2300000, "1년 이상",
                    LocalTime.of(6, 30), LocalTime.of(15, 30),
                    Set.of(Workday.MON, Workday.TUE, Workday.WED, Workday.THU, Workday.FRI),
                    ExposureType.LIGHT, 7, true),
            new Template("ABC마트", "매장 판매·재고 관리", 4, PayType.HOURLY, 11000, "1개월~3개월",
                    LocalTime.of(13, 0), LocalTime.of(22, 0), Set.of(Workday.FRI, Workday.SAT, Workday.SUN),
                    ExposureType.NORMAL, 40, false),
            new Template("아웃백스테이크하우스", "홀 서빙 주말 파트타이머", 1, PayType.DAILY, 110000, "1주일~1개월",
                    LocalTime.of(11, 0), LocalTime.of(21, 0), Set.of(Workday.SAT, Workday.SUN),
                    ExposureType.NORMAL, 400, false),
    };

    private static final Template CLOSED_TEMPLATE =
            new Template("다이소", "[마감] 매장 관리 스태프", 5, PayType.HOURLY, 10030, "3개월~6개월",
                    LocalTime.of(10, 0), LocalTime.of(19, 0), Set.of(Workday.MON, Workday.WED, Workday.FRI),
                    ExposureType.NORMAL, 0, false);
}