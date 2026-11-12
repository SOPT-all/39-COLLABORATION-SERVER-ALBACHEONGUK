package org.sopt.alba.domain.recommendation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.alba.common.exception.BusinessException;
import org.sopt.alba.domain.job.code.JobErrorCode;
import org.sopt.alba.domain.job.dto.response.JobSummaryResponse;
import org.sopt.alba.domain.job.entity.JobPosting;
import org.sopt.alba.domain.job.entity.Region;
import org.sopt.alba.domain.job.repository.RegionRepository;
import org.sopt.alba.domain.member.code.MemberErrorCode;
import org.sopt.alba.domain.member.dto.response.ResumeSummaryResponse;
import org.sopt.alba.domain.member.entity.Resume;
import org.sopt.alba.domain.member.repository.ApplicationRepository;
import org.sopt.alba.domain.member.repository.MemberRepository;
import org.sopt.alba.domain.member.repository.ResumeRepository;
import org.sopt.alba.domain.recommendation.code.RecommendationErrorCode;
import org.sopt.alba.domain.recommendation.dto.request.ResumeRecommendationRequest;
import org.sopt.alba.domain.recommendation.dto.response.RegionRecommendationResponse;
import org.sopt.alba.domain.recommendation.dto.response.ResumeRecommendationResponse;
import org.sopt.alba.domain.recommendation.enums.RecommendationStatus;
import org.sopt.alba.domain.recommendation.repository.RecommendationQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sopt.alba.domain.job.enums.Workday;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int REGION_RECOMMENDATION_SIZE = 4;
    private static final int SIDO_DEPTH = 1;

    private final MemberRepository memberRepository;
    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;
    private final ResumeMatchScorer resumeMatchScorer;
    private final RegionRepository regionRepository;
    private final RecommendationQueryRepository recommendationQueryRepository;
    private final Clock clock;

    private static final int RESUME_RECOMMENDATION_SIZE = 3;

    private static final Comparator<ScoredPosting> BY_MATCH = Comparator
            .comparingInt((ScoredPosting scored) -> scored.score().total()).reversed()
            .thenComparing(scored -> scored.posting().getPublishedAt(), Comparator.reverseOrder())
            .thenComparing(scored -> scored.posting().getId(), Comparator.reverseOrder());

    /* 시/도 기준으로 지역별 공고를 조회 */
    public RegionRecommendationResponse getRegionRecommendations(Long sidoId) {
        Region sido = regionRepository.findById(sidoId)
                .orElseThrow(() ->
                        new BusinessException(JobErrorCode.REGION_NOT_FOUND));

        if (sido.getDepth() != SIDO_DEPTH) {
            throw new BusinessException(RecommendationErrorCode.INVALID_SIDO);
        }

        LocalDate today = LocalDate.now(clock);

        List<JobSummaryResponse> jobs = recommendationQueryRepository
                .findOpenBySido(sidoId, today, REGION_RECOMMENDATION_SIZE)
                .stream()
                .map(posting -> JobSummaryResponse.of(posting, today))
                .toList();

        return new RegionRecommendationResponse(
                sido.getId(),
                sido.getName(),
                jobs
        );
    }

    //이력서 매칭 공고 추천 + memberId가 null이면 guest.
    public ResumeRecommendationResponse getResumeRecommendations(Long memberId, ResumeRecommendationRequest request) {
        if (memberId == null) {
            return ResumeRecommendationResponse.guest();
        }
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        List<Resume> resumes = resumeRepository.findAllByMemberIdOrderByModifiedAtDescIdDesc(memberId);
        Resume selected = request.resumeId() == null ? null : getSelectableResume(memberId, request.resumeId());

        Map<Long, Region> regionsById = findDesiredRegions(resumes);
        List<ResumeSummaryResponse> resumeOptions = resumes.stream()
                .map(resume -> toSummary(resume, regionsById))
                .toList();

        if (resumes.isEmpty()) {
            return ResumeRecommendationResponse.withoutBase(RecommendationStatus.NO_RESUME, resumeOptions);
        }
        List<Resume> completed = resumes.stream().filter(Resume::isCompleted).toList();
        if (completed.isEmpty()) {
            return ResumeRecommendationResponse.withoutBase(RecommendationStatus.DRAFT_ONLY, resumeOptions);
        }

        Resume base = selected != null ? selected : chooseBaseResume(completed, request);
        List<JobSummaryResponse> jobs = recommend(memberId, base, request);

        return ResumeRecommendationResponse.of(toSummary(base, regionsById), resumeOptions, jobs);
    }

    private Resume getSelectableResume(Long memberId, Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.RESUME_NOT_FOUND));
        if (!resume.isOwnedBy(memberId)) {
            throw new BusinessException(RecommendationErrorCode.RESUME_NOT_OWNED);
        }
        if (!resume.isCompleted()) {
            throw new BusinessException(RecommendationErrorCode.RESUME_NOT_COMPLETED);
        }
        return resume;
    }

    private Resume chooseBaseResume(List<Resume> completed, ResumeRecommendationRequest request) {
        Resume latest = completed.get(0);

        Set<String> searchCategoryCodes = resolveSearchCategoryCodes(request);
        if (searchCategoryCodes.isEmpty()) {
            return latest;
        }

        Map<String, String> parentCodes = recommendationQueryRepository.findCategoryParentCodes();
        return completed.stream()
                .filter(resume -> resume.getDesiredCategoryCodes().stream()
                        .anyMatch(desired -> isRelatedCategory(desired, searchCategoryCodes, parentCodes)))
                .findFirst()
                .orElse(latest);
    }

    private Set<String> resolveSearchCategoryCodes(ResumeRecommendationRequest request) {
        if (!request.categoryCodes().isEmpty()) {
            return new HashSet<>(request.categoryCodes());
        }
        if (request.keyword() != null && !request.keyword().isEmpty()) {
            return new HashSet<>(recommendationQueryRepository.findCategoryCodesByKeyword(request.keyword()));
        }
        return Set.of();
    }

    private boolean isRelatedCategory(String desired, Set<String> searchCodes, Map<String, String> parentCodes) {
        if (searchCodes.contains(desired)) {
            return true;
        }
        String desiredParent = parentCodes.get(desired);
        if (desiredParent != null && searchCodes.contains(desiredParent)) {
            return true;
        }
        return searchCodes.stream().anyMatch(searchCode -> desired.equals(parentCodes.get(searchCode)));
    }

    private List<JobSummaryResponse> recommend(Long memberId, Resume base, ResumeRecommendationRequest request) {
        LocalDate today = LocalDate.now(clock);
        List<Long> appliedJobIds = applicationRepository.findAppliedJobPostingIds(memberId);

        List<Long> regionIds = request.regionIds().isEmpty() ? base.getDesiredRegionIds() : request.regionIds();

        // List<String> -> List<Workday> 변환 추가
        List<Workday> workdays = request.workdays().stream()
                .map(Workday::valueOf)
                .toList();

        List<JobPosting> candidates = recommendationQueryRepository.findMatchCandidates(
                today, appliedJobIds, regionIds, request.keyword(), request.categoryCodes(), workdays);

        List<ScoredPosting> recommended = candidates.stream()
                .filter(JobPosting::isOnlineApplicable)
                .map(posting -> new ScoredPosting(posting, resumeMatchScorer.score(base, posting)))
                .filter(scored -> scored.score().isRecommendable())
                .sorted(BY_MATCH)
                .limit(RESUME_RECOMMENDATION_SIZE)
                .toList();

        recommended.forEach(scored -> log.info("[ResumeMatch] resumeId={} jobId={} base={} bonus={} total={}",
                base.getId(), scored.posting().getId(), scored.score().base(), scored.score().bonus(),
                scored.score().total()));

        return recommended.stream()
                .map(scored -> JobSummaryResponse.of(scored.posting(), today))
                .toList();
    }

    /* 이력서들의 희망 근무지역을 한 번에 조회 (드롭다운의 희망 근무지역 표시용) */
    private Map<Long, Region> findDesiredRegions(List<Resume> resumes) {
        Set<Long> regionIds = resumes.stream()
                .flatMap(resume -> resume.getDesiredRegionIds().stream())
                .collect(Collectors.toSet());
        return regionRepository.findAllById(regionIds).stream()
                .collect(Collectors.toMap(Region::getId, Function.identity()));
    }

    private ResumeSummaryResponse toSummary(Resume resume, Map<Long, Region> regionsById) {
        List<String> regionNames = resume.getDesiredRegionIds().stream()
                .map(regionsById::get)
                .filter(region -> region != null)
                .map(Region::getFullName)
                .toList();
        return ResumeSummaryResponse.of(resume, regionNames);
    }

    private record ScoredPosting(JobPosting posting, ResumeMatchScorer.MatchScore score) {
    }
}