package org.sopt.alba.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** List<Long> 을 "11,12,13" (콤마 문자열)로 -> 이거 필요 없으면 파일 지우기 */

@Converter
public class LongListConverter implements AttributeConverter<List<Long>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.stream().map(String::valueOf).collect(Collectors.joining(DELIMITER));
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        return Arrays.stream(dbData.split(DELIMITER))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }
}