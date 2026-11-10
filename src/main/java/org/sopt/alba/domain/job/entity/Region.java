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

/* 지역 2단계: depth 1 = 시/도, depth 2 = 시/군/구 (parent = 시/도) */
@Getter
@Entity
@Table(name = "region")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

    @Id
    private Long id;               // 더미 시트의 지역 ID를 그대로 사용

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private int depth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Region parent;

    private Region(Long id, String name, int depth, Region parent) {
        this.id = id;
        this.name = name;
        this.depth = depth;
        this.parent = parent;
    }

    public static Region sido(Long id, String name) {
        return new Region(id, name, 1, null);
    }

    public static Region sigungu(Long id, String name, Region sido) {
        return new Region(id, name, 2, sido);
    }

    public String getFullName() {
        return parent == null ? name : parent.getName() + " " + name;
    }
}