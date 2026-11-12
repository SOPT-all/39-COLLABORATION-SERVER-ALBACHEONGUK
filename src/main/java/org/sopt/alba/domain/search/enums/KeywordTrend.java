package org.sopt.alba.domain.search.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum KeywordTrend {
    UP,
    DOWN,
    STEADY,
    NEW;

    @JsonValue
    public String getValue() {
        return this == STEADY ? "SAME" : name();
    }
}
