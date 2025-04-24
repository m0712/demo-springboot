package com.example.excel2db.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class DataCleaner {

    private static final List<String> EMAIL_COLUMNS = Arrays.asList("email", "e-mail", "mail");
    private static final List<String> PHONE_COLUMNS = Arrays.asList("phone", "mobile", "telephone");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s./0-9]*$");

    public Object cleanData(String columnName, Object value) {
        if (value == null) {
            return null;
        }

        // Convert to proper type
        if (value instanceof String) {
            String strValue = ((String) value).trim();
            if (StringUtils.isBlank(strValue)) {
                return null;
            }

            // Specific cleaning based on column name
            if (isEmailColumn(columnName)) {
                return cleanEmail(strValue);
            } else if (isPhoneColumn(columnName)) {
                return cleanPhoneNumber(strValue);
            }
            return strValue;
        } else if (value instanceof Double) {
            return BigDecimal.valueOf((Double) value);
        } else if (value instanceof LocalDateTime) {
            return value;
        } else if (value instanceof Boolean) {
            return value;
        }

        return value;
    }

    private boolean isEmailColumn(String columnName) {
        return EMAIL_COLUMNS.contains(columnName.toLowerCase());
    }

    private boolean isPhoneColumn(String columnName) {
        return PHONE_COLUMNS.contains(columnName.toLowerCase());
    }

    private String cleanEmail(String email) {
        email = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        return email;
    }

    private String cleanPhoneNumber(String phone) {
        phone = phone.replaceAll("[^+0-9]", "");
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Invalid phone number format: " + phone);
        }
        return phone;
    }
}