package org.sopt.alba.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.alba.domain.job.entity.JobPosting;

/* 지원 내역 (더미). 이력서 매칭에서 이미 지원한 공고 제외하기 위해 넣음 */

@Getter
@Entity
@Table(name = "application",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "job_posting_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    private Application(Member member, JobPosting jobPosting, LocalDateTime appliedAt) {
        this.member = member;
        this.jobPosting = jobPosting;
        this.appliedAt = appliedAt;
    }

    public static Application create(Member member, JobPosting jobPosting, LocalDateTime appliedAt) {
        return new Application(member, jobPosting, appliedAt);
    }
}