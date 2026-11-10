package model;

import util.ValidationException;

/**
 * Domain entity representing an authenticated cybersecurity operator or analyst.
 */
public class User {

    private int userId;
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String role; // "ADMIN", "ANALYST", "OPERATOR"
    private String createdAt;

    public User() {
        this.role = "ANALYST";
    }

    public User(int userId, String username, String passwordHash, String salt,
                String fullName, String role, String createdAt) throws ValidationException {
        this.userId = userId;
        setUsername(username);
        this.passwordHash = passwordHash;
        this.salt = salt;
        setFullName(fullName);
        setRole(role);
        this.createdAt = (createdAt == null || createdAt.isBlank()) ? java.time.LocalDateTime.now().toString() : createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) throws ValidationException {
        if (username == null || username.trim().length() < 3) {
            throw new ValidationException("username", "Username must be at least 3 characters long");
        }
        this.username = username.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) throws ValidationException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("fullName", "Full name cannot be empty");
        }
        this.fullName = fullName.trim();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) throws ValidationException {
        if (role == null) {
            throw new ValidationException("role", "Role cannot be null");
        }
        String upper = role.trim().toUpperCase();
        if (!upper.equals("ADMIN") && !upper.equals("ANALYST") && !upper.equals("OPERATOR")) {
            throw new ValidationException("role", "Role must be ADMIN, ANALYST, or OPERATOR. Given: " + role);
        }
        this.role = upper;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

