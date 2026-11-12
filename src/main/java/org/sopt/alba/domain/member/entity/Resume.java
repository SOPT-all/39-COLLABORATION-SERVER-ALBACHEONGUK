package org.sopt.alba.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.alba.common.converter.LongListConverter;
import org.sopt.alba.common.converter.StringListConverter;
import org.sopt.alba.domain.job.enums.EmploymentType;
import org.sopt.alba.domain.job.enums.LanguageLevel;
import org.sopt.alba.domain.job.enums.WorkPeriod;
import org.sopt.alba.domain.job.enums.Workday;
import org.sopt.alba.domain.member.enums.ResumeStatus;

// TODO 기획 더미 시트의 이력서 컬럼이 확정되면 필드와 형식을 맞춘다.

@Getter
@Entity
@Table(name = "resume")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResumeStatus status;

    /* 희망 근무지역 */
    @Convert(converter = LongListConverter.class)
    @Column(name = "desired_region_ids", length = 500)
    private List<Long> desiredRegionIds = new ArrayList<>();

    /* 희망 업직종*/
    @Convert(converter = StringListConverter.class)
    @Column(name = "desired_category_codes", length = 500)
    private List<String> desiredCategoryCodes = new ArrayList<>();

    @Column(name = "workdays_any", nullable = false)
    private boolean workdaysAny;                   // 요일무관

    @Convert(converter = StringListConverter.class)
    @Column(name = "desired_workdays", length = 100)
    private List<String> desiredWorkdays = new ArrayList<>();

    @Column(name = "work_period_any", nullable = false)
    private boolean workPeriodAny;                 // 기간무관

    @Convert(converter = StringListConverter.class)
    @Column(name = "desired_work_periods", length = 300)
    private List<String> desiredWorkPeriods = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "desired_employment_types", length = 200)
    private List<String> desiredEmploymentTypes = new ArrayList<>();

    // TODO 경력이 여러 개일 수 있으면 별도 테이블로 분리
    @Column(name = "career_category_code", length = 20)
    private String careerCategoryCode;             // 경력 업직종 코드

    @Column(name = "career_months", nullable = false)
    private int careerMonths;

    @Convert(converter = StringListConverter.class)
    @Column(length = 500)
    private List<String> skills = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(length = 500)
    private List<String> certificates = new ArrayList<>();

    @Column(length = 30)
    private String language;                       // 예: "영어" (없으면 null)

    @Enumerated(EnumType.STRING)
    @Column(name = "language_level", length = 20)
    private LanguageLevel languageLevel;

    @Column(name = "computer_skill", nullable = false)
    private boolean computerSkill;

    @Column(name = "portfolio_category_code", length = 20)
    private String portfolioCategoryCode;          // 포트폴리오 분야(업직종 코드). 포트폴리오 없으면 null

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;              // 기준 이력서 = COMPLETED 중 modifiedAt 최신(같으면 id 큰 것)

    @Builder
    private Resume(Member member, String title, ResumeStatus status, List<Long> desiredRegionIds,
                   List<String> desiredCategoryCodes, boolean workdaysAny, Set<Workday> desiredWorkdays,
                   boolean workPeriodAny, Set<WorkPeriod> desiredWorkPeriods,
                   Set<EmploymentType> desiredEmploymentTypes, String careerCategoryCode, int careerMonths,
                   List<String> skills, List<String> certificates, String language, LanguageLevel languageLevel,
                   boolean computerSkill, String portfolioCategoryCode, LocalDateTime modifiedAt) {
        this.member = member;
        this.title = title;
        this.status = status;
        this.desiredRegionIds = copy(desiredRegionIds);
        this.desiredCategoryCodes = copy(desiredCategoryCodes);
        this.workdaysAny = workdaysAny;
        this.desiredWorkdays = names(desiredWorkdays);
        this.workPeriodAny = workPeriodAny;
        this.desiredWorkPeriods = names(desiredWorkPeriods);
        this.desiredEmploymentTypes = names(desiredEmploymentTypes);
        this.careerCategoryCode = careerCategoryCode;
        this.careerMonths = careerMonths;
        this.skills = copy(skills);
        this.certificates = copy(certificates);
        this.language = language;
        this.languageLevel = languageLevel;
        this.computerSkill = computerSkill;
        this.portfolioCategoryCode = portfolioCategoryCode;
        this.modifiedAt = modifiedAt;
    }

    public boolean isCompleted() {
        return status == ResumeStatus.COMPLETED;
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }

    public Set<Workday> getDesiredWorkdaySet() {
        return desiredWorkdays.stream().map(Workday::valueOf).collect(Collectors.toSet());
    }

    public Set<WorkPeriod> getDesiredWorkPeriodSet() {
        return desiredWorkPeriods.stream().map(WorkPeriod::valueOf).collect(Collectors.toSet());
    }

    public Set<EmploymentType> getDesiredEmploymentTypeSet() {
        return desiredEmploymentTypes.stream().map(EmploymentType::valueOf).collect(Collectors.toSet());
    }

    private static <T> List<T> copy(List<T> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    private static List<String> names(Collection<? extends Enum<?>> values) {
        return values == null
                ? new ArrayList<>()
                : values.stream().map(Enum::name).sorted().collect(Collectors.toCollection(ArrayList::new));
    }
}