package org.sopt.alba.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;

/** List<String> 을 "A01,A02" (콤마 문자열)로. 얘도 필요 없으면 지우기 */

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return String.join(DELIMITER, attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        return Arrays.stream(dbData.split(DELIMITER))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}