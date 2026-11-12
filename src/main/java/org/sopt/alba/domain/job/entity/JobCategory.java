package org.sopt.alba.domain.job.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/* 업직종 2단계: parent == null 이면 대분류, 아니면 소분류 */

@Getter
@Entity
@Table(name = "job_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobCategory {

    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_code")
    private JobCategory parent;

    private JobCategory(String code, String name, JobCategory parent) {
        this.code = code;
        this.name = name;
        this.parent = parent;
    }

    public static JobCategory main(String code, String name) {
        return new JobCategory(code, name, null);
    }

    public static JobCategory sub(String code, String name, JobCategory parent) {
        return new JobCategory(code, name, parent);
    }
}