package org.sopt.alba.domain.member.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import org.sopt.alba.domain.member.entity.Resume;
import org.sopt.alba.domain.member.enums.ResumeStatus;

/* 이력서 드롭다운 1개 항목 */
public record ResumeSummaryResponse(
        Long resumeId,
        String title,
        ResumeStatus status,
        LocalDateTime modifiedAt,
        List<String> desiredRegionNames
) {

    public static ResumeSummaryResponse of(Resume resume, List<String> desiredRegionNames) {
        return new ResumeSummaryResponse(
                resume.getId(), resume.getTitle(), resume.getStatus(), resume.getModifiedAt(), desiredRegionNames);
    }
}