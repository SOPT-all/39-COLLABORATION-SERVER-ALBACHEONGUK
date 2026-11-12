package org.sopt.alba.domain.job.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import org.sopt.alba.domain.job.enums.ExposureType;
import org.sopt.alba.domain.job.enums.PayType;
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
    @JoinColumn(name = "region_id", nullable = false)        // 시·군·구
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_category_code", nullable = false)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayType payType;

    @Column(nullable = false)
    private int payAmount;

    @Column(length = 30)
    private String workPeriod;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(nullable = false)
    private boolean daysNegotiable;

    @Column(nullable = false)
    private boolean timeNegotiable;

    private LocalDate deadline;        // null이면 상시모집

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExposureType exposureType;

    @Column(nullable = false)
    private LocalDate publishedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "posting_workday", joinColumns = @JoinColumn(name = "job_posting_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "workday", length = 10)
    private Set<Workday> workdays = new HashSet<>();

    @Builder
    private JobPosting(Region region, JobCategory jobCategory, String companyName, String title,
                       String dong, String addressDetail, String nearbySubway, String imageUrl,
                       String applyMethod, PayType payType, int payAmount, String workPeriod,
                       LocalTime startTime, LocalTime endTime, boolean daysNegotiable,
                       boolean timeNegotiable, LocalDate deadline, ExposureType exposureType,
                       LocalDate publishedAt, Set<Workday> workdays) {
        this.region = region;
        this.jobCategory = jobCategory;
        this.companyName = companyName;
        this.title = title;
        this.dong = dong;
        this.addressDetail = addressDetail;
        this.nearbySubway = nearbySubway;
        this.imageUrl = imageUrl;
        this.applyMethod = applyMethod;
        this.payType = payType;
        this.payAmount = payAmount;
        this.workPeriod = workPeriod;
        this.startTime = startTime;
        this.endTime = endTime;
        this.daysNegotiable = daysNegotiable;
        this.timeNegotiable = timeNegotiable;
        this.deadline = deadline;
        this.exposureType = exposureType;
        this.publishedAt = publishedAt;
        this.workdays = (workdays == null || workdays.isEmpty())
                ? new HashSet<>() : EnumSet.copyOf(workdays);
    }

    /* 마감 = 마감일 < 오늘, 마감일이 없으면 상시모집이라 마감 아님 */
    public boolean isClosed(LocalDate today) {
        return deadline != null && deadline.isBefore(today);
    }
}