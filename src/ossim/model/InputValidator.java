package ossim.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized input validation and parsing utilities for all simulator modules.
 * Ensures blank fields, non-numeric text, negative values where disallowed, and
 * out-of-range parameters are caught gracefully with actionable error messages.
 */
public final class InputValidator {

    private InputValidator() {
    }

    public static String requireNonBlank(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be blank.");
        }
        return value.trim();
    }

    public static int parseInt(String value, String fieldName) throws ValidationException {
        String trimmed = requireNonBlank(value, fieldName);
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            throw new ValidationException(fieldName + " must be a valid integer (got \"" + trimmed + "\").");
        }
    }

    public static int parseIntInRange(String value, String fieldName, int min, int max) throws ValidationException {
        int parsed = parseInt(value, fieldName);
        if (parsed < min || parsed > max) {
            throw new ValidationException(
                    fieldName + " must be between " + min + " and " + max + " (got " + parsed + ").");
        }
        return parsed;
    }

    public static int parseNonNegativeInt(String value, String fieldName) throws ValidationException {
        int parsed = parseInt(value, fieldName);
        if (parsed < 0) {
            throw new ValidationException(fieldName + " cannot be negative (got " + parsed + ").");
        }
        return parsed;
    }

    public static int parsePositiveInt(String value, String fieldName) throws ValidationException {
        int parsed = parseInt(value, fieldName);
        if (parsed <= 0) {
            throw new ValidationException(fieldName + " must be greater than 0 (got " + parsed + ").");
        }
        return parsed;
    }

    public static long parseNonNegativeLong(String value, String fieldName) throws ValidationException {
        String trimmed = requireNonBlank(value, fieldName);
        try {
            long parsed = Long.parseLong(trimmed);
            if (parsed < 0) {
                throw new ValidationException(fieldName + " cannot be negative (got " + parsed + ").");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new ValidationException(fieldName + " must be a valid integer (got \"" + trimmed + "\").");
        }
    }

    public static long parsePositiveLong(String value, String fieldName) throws ValidationException {
        long parsed = parseNonNegativeLong(value, fieldName);
        if (parsed <= 0) {
            throw new ValidationException(fieldName + " must be greater than 0 (got " + parsed + ").");
        }
        return parsed;
    }

    /**
     * Parses a variable-length list of integers separated by commas, spaces, semicolons, or newlines.
     */
    public static List<Integer> parseIntList(String text, String fieldName, int minAllowed, int maxAllowed)
            throws ValidationException {
        String trimmed = requireNonBlank(text, fieldName);
        String[] tokens = trimmed.split("[,;\\s]+");
        List<Integer> values = new ArrayList<>(tokens.length);
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (token.isEmpty()) {
                continue;
            }
            try {
                int val = Integer.parseInt(token);
                if (val < minAllowed || val > maxAllowed) {
                    throw new ValidationException(
                            fieldName + " item #" + (values.size() + 1) + " (" + val + ") must be between "
                                    + minAllowed + " and " + maxAllowed + ".");
                }
                values.add(val);
            } catch (NumberFormatException ex) {
                throw new ValidationException(
                        fieldName + " contains invalid integer \"" + token + "\" at item #" + (values.size() + 1) + ".");
            }
        }
        if (values.isEmpty()) {
            throw new ValidationException(fieldName + " must contain at least one integer.");
        }
        return values;
    }
}
