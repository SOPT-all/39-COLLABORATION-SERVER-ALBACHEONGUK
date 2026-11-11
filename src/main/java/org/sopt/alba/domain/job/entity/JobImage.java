package org.sopt.alba.domain.job.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Read-only image projection; shares PR #11/#14's job_posting table. */
@Getter
@Entity
@Immutable
@Table(name = "job_posting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobImage {
    @Id
    private Long id;

    @Column(length = 500)
    private String imageUrl;
}
