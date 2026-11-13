package org.sopt.alba.domain.job.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.sopt.alba.common.code.GlobalErrorCode;
import org.sopt.alba.common.exception.BusinessException;
import org.sopt.alba.domain.job.dto.request.JobSearchRequest;
import org.sopt.alba.domain.job.dto.response.JobSearchResponse;
import org.sopt.alba.domain.job.enums.JobPayTypeCode;
import org.sopt.alba.domain.job.enums.PayType;
import org.sopt.alba.domain.job.repository.JobCategoryRepository;
import org.sopt.alba.domain.job.repository.JobSearchRepository;
import org.sopt.alba.domain.job.repository.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobSearchService {

    private static final Set<Integer> PAGE_SIZES = Set.of(20, 30, 50);
    private static final Set<String> ONGOING_VALUES = Set.of("true", "false");
    private static final Pattern SPECIAL_CHARACTERS = Pattern.compile("[^\\p{L}\\p{N}\\s]");

    private final JobSearchRepository jobSearchRepository;
    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final Clock clock;

    public JobSearchResponse findJobs(JobSearchRequest request) {
        if (!ONGOING_VALUES.contains(request.ongoing()) || !request.workTimeCodes().isEmpty()) {
            throw new BusinessException(GlobalErrorCode.INVALID_PARAMETER_TYPE);
        }
        PayType payType = findPayType(request.payType());
        validateFilters(request);

        String keyword = request.keyword() == null ? "" :
                SPECIAL_CHARACTERS.matcher(request.keyword().toLowerCase(Locale.ROOT)).replaceAll("").strip();
        LocalDate today = LocalDate.now(clock);
        return JobSearchResponse.of(request, jobSearchRepository.findJobs(request, keyword, payType, today), today);
    }

    private void validateFilters(JobSearchRequest request) {
        Integer minPay = request.minPay();
        Integer maxPay = request.maxPay();
        if (!PAGE_SIZES.contains(request.size())
                || (minPay != null && maxPay != null && minPay > maxPay)
                || ((minPay != null || maxPay != null) && request.payType() == null)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }

        Set<Long> regionIds = new HashSet<>(request.regionIds());
        Set<String> categoryCodes = new HashSet<>(request.categoryCodes());
        if ((!regionIds.isEmpty() && regionRepository.findAllById(regionIds).size() != regionIds.size())
                || (!categoryCodes.isEmpty()
                && jobCategoryRepository.findAllById(categoryCodes).size() != categoryCodes.size())) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
    }

    private PayType findPayType(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(PayType.values())
                .filter(payType -> JobPayTypeCode.toApiCode(payType).equals(code))
                .findFirst()
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.INVALID_PARAMETER_TYPE));
    }
}
