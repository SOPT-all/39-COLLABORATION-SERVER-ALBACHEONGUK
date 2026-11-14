package org.sopt.alba.domain.job.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.alba.common.entity.BaseTimeEntity;
import org.sopt.alba.domain.job.enums.EmploymentType;
import org.sopt.alba.domain.job.enums.ExposureType;
import org.sopt.alba.domain.job.enums.PayType;
import org.sopt.alba.domain.job.enums.RecruitmentStatus;
import org.sopt.alba.domain.job.enums.WorkPeriod;
import org.sopt.alba.domain.job.enums.Workday;

@Getter
@Entity
@Table(name = "job_posting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPosting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)        // 시,군,구
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_category_code", nullable = false) // 소분류
    private JobCategory jobCategory;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    private String dong;

    @Column(length = 200)
    private String addressDetail;

    @Column(length = 100)
    private String nearbySubway;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 100)
    private String applyMethod;

    @Column(name = "in_service_apply", nullable = false)
    private boolean inServiceApply = true; // (데이터) 지원방법과 서비스 내 지원 가능 여부를 분리한다.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayType payType;

    @Column
    private Integer payAmount; // (데이터) 급여 협의 공고는 금액을 null로 저장한다.

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private WorkPeriod workPeriod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentType employmentType;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(nullable = false)
    private boolean daysNegotiable;

    @Column(nullable = false)
    private boolean timeNegotiable;

    private LocalDate deadline;        // null이면 상시모집

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecruitmentStatus recruitmentStatus = RecruitmentStatus.RECRUITING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExposureType exposureType;

    @Column(nullable = false)
    private LocalDate publishedAt;     // 등록일 (n일 전 라벨 기준)

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "posting_workday", joinColumns = @JoinColumn(name = "job_posting_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "workday", length = 10)
    private Set<Workday> workdays = new HashSet<>();

    @Embedded
    private JobRequirement requirement = JobRequirement.none();   // 경력,우대 조건 (이력서 매칭용)

    @Builder
    private JobPosting(Region region, JobCategory jobCategory, String companyName, String title,
                       String dong, String addressDetail, String nearbySubway, String imageUrl,
                       String applyMethod, Boolean inServiceApply, PayType payType, Integer payAmount, WorkPeriod workPeriod,
                       EmploymentType employmentType,
                       LocalTime startTime, LocalTime endTime, boolean daysNegotiable,
                       boolean timeNegotiable, LocalDate deadline, ExposureType exposureType,
                       LocalDate publishedAt, Set<Workday> workdays, JobRequirement requirement,
                       RecruitmentStatus recruitmentStatus) {
        this.region = region;
        this.jobCategory = jobCategory;
        this.companyName = companyName;
        this.title = title;
        this.dong = dong;
        this.addressDetail = addressDetail;
        this.nearbySubway = nearbySubway;
        this.imageUrl = imageUrl;
        this.applyMethod = applyMethod;
        this.inServiceApply = inServiceApply == null || inServiceApply; // (데이터) 기존 공고의 기본값은 지원 가능이다.
        this.payType = payType;
        this.payAmount = payAmount;
        this.workPeriod = workPeriod;
        this.employmentType = employmentType == null ? EmploymentType.PART_TIME : employmentType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.daysNegotiable = daysNegotiable;
        this.timeNegotiable = timeNegotiable;
        this.deadline = deadline;
        this.recruitmentStatus = recruitmentStatus == null
                ? RecruitmentStatus.RECRUITING : recruitmentStatus;
        this.exposureType = exposureType;
        this.publishedAt = publishedAt;
        this.workdays = (workdays == null || workdays.isEmpty())
                ? new HashSet<>() : EnumSet.copyOf(workdays);
        this.requirement = requirement == null ? JobRequirement.none() : requirement;
    }

    public JobRequirement getRequirement() {
        return requirement == null ? JobRequirement.none() : requirement;
    }

    // (데이터) 추천 포함 여부는 양식의 전용 가능 필드로 판단한다.
    public boolean isInServiceApplicable() {
        return inServiceApply;
    }

    /* 기업의 채용 완료 상태 또는 마감일 경과 시 마감 */
    public boolean isClosed(LocalDate today) {
        return recruitmentStatus == RecruitmentStatus.COMPLETED
                || (deadline != null && deadline.isBefore(today));
    }
}
