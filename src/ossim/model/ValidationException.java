package ossim.model;

/**
 * Custom checked exception thrown when user-supplied simulator inputs fail validation.
 * Carries a clear, user-friendly message explaining what field or value is invalid.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
