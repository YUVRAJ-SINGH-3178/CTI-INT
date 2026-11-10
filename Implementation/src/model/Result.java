package model;

/**
 * Generic response wrapper demonstrating Java Generics.
 * Encapsulates operation status, generic payload data T, error diagnostics, and timestamps.
 *
 * @param <T> Type of the encapsulated data entity
 */
public class Result<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final long timestamp;

    private Result(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(true, data, message);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, "Operation completed successfully");
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(false, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("Result{success=%s, message='%s', timestamp=%d}", success, message, timestamp);
    }
}

