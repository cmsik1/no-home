package com.ssafy.home.common.persistence;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * native query가 반환한 비정형 값을 DTO 필드 타입으로 안전하게 바꾸는 공통 변환기다.
 * DB driver가 숫자·날짜를 다른 구체 타입으로 반환하더라도 row mapper가 같은 규칙을 사용하게 한다.
 */
public final class JpaRows {

    private JpaRows() {
    }

    public static Long longValue(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    public static Integer intValue(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    public static BigDecimal decimalValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }

    public static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public static LocalDate dateValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(String.valueOf(value));
    }

    public static LocalDateTime dateTimeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return LocalDateTime.parse(String.valueOf(value).replace(' ', 'T'));
    }
}
