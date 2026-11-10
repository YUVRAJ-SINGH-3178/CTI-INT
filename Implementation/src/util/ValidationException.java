package util;

/**
 * Custom checked exception thrown when domain validation rules are violated.
 */
public class ValidationException extends Exception {
    private final String fieldName;

    public ValidationException(String message) {
        super(message);
        this.fieldName = "UNKNOWN";
    }

    public ValidationException(String fieldName, String message) {
        super("Validation error on field '" + fieldName + "': " + message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}

