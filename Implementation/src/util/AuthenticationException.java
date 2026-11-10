package util;

/**
 * Custom checked exception thrown when user authentication fails.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}

