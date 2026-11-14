package org.sopt.alba.domain.job.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.alba.common.converter.StringListConverter;
import org.sopt.alba.domain.job.enums.CareerRequirement;
import org.sopt.alba.domain.job.enums.LanguageLevel;

/*
 * 공고의 경력/우대 조건. job_posting 테이블에 컬럼으로 같이 저장된다. (@Embedded)
 * 이력서 매칭 점수 계산에만 쓰고, 목록 카드 응답에는 내려가지 않습니다.
 */


@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobRequirement {

    @Enumerated(EnumType.STRING)
    @Column(name = "career_requirement", nullable = false, length = 20)
    private CareerRequirement careerRequirement = CareerRequirement.ANY;

    @Column(name = "career_months", nullable = false)
    private int careerMonths;                       // REQUIRED / PREFERRED 일 때 기준 개월 수

    @Convert(converter = StringListConverter.class)
    @Column(name = "preferred_skills", length = 500)
    private List<String> preferredSkills = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "preferred_certificates", length = 500)
    private List<String> preferredCertificates = new ArrayList<>();

    @Column(name = "preferred_language", length = 30)
    private String preferredLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language_level", length = 20)
    private LanguageLevel preferredLanguageLevel;

    @Column(name = "computer_skill_preferred", nullable = false)
    private boolean computerSkillPreferred;

    @Convert(converter = StringListConverter.class)
    @Column(name = "preferred_computer_skills", length = 500)
    private List<String> preferredComputerSkills = new ArrayList<>(); // (데이터) 엑셀 등 구체적인 우대 항목

    @Column(name = "portfolio_preferred", nullable = false)
    private boolean portfolioPreferred;

    @Builder
    private JobRequirement(CareerRequirement careerRequirement, int careerMonths, List<String> preferredSkills,
                           List<String> preferredCertificates, String preferredLanguage,
                           LanguageLevel preferredLanguageLevel, boolean computerSkillPreferred,
                           List<String> preferredComputerSkills,
                           boolean portfolioPreferred) {
        this.careerRequirement = careerRequirement == null ? CareerRequirement.ANY : careerRequirement;
        this.careerMonths = careerMonths;
        this.preferredSkills = preferredSkills == null ? new ArrayList<>() : new ArrayList<>(preferredSkills);
        this.preferredCertificates =
                preferredCertificates == null ? new ArrayList<>() : new ArrayList<>(preferredCertificates);
        this.preferredLanguage = preferredLanguage;
        this.preferredLanguageLevel = preferredLanguageLevel;
        this.computerSkillPreferred = computerSkillPreferred;
        this.preferredComputerSkills = preferredComputerSkills == null
                ? new ArrayList<>() : new ArrayList<>(preferredComputerSkills); // (데이터)
        this.portfolioPreferred = portfolioPreferred;
    }

    /* 조건이 없는 공고: 경력무관, 우대사항 없음 */
    public static JobRequirement none() {
        return JobRequirement.builder().careerRequirement(CareerRequirement.ANY).build();
    }
}
