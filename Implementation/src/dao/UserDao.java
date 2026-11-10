package dao;

import model.User;
import util.DatabaseConnection;
import util.DataAccessException;
import util.PasswordSecurity;
import util.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User authentication and role management.
 */
public class UserDao implements Repository<User, Integer> {

    @Override
    public User save(User user) throws DataAccessException, ValidationException {
        if (user == null) {
            throw new ValidationException("user", "User cannot be null");
        }

        String sql = "INSERT INTO users (username, password_hash, salt, full_name, role, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getSalt());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getCreatedAt());

            pstmt.executeUpdate();
            try (ResultSet gk = pstmt.getGeneratedKeys()) {
                if (gk.next()) {
                    user.setUserId(gk.getInt(1));
                }
            }
            return user;

        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user '" + user.getUsername() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Integer userId) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by ID " + userId + ": " + e.getMessage(), e);
        }
    }

    public Optional<User> findByUsername(String username) throws DataAccessException {
        if (username == null) return Optional.empty();
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Error finding user '" + username + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() throws DataAccessException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all users: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(User user) throws DataAccessException, ValidationException {
        if (user == null || user.getUserId() <= 0) {
            throw new ValidationException("userId", "Valid user ID required for update");
        }

        String sql = "UPDATE users SET username = ?, password_hash = ?, salt = ?, full_name = ?, role = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getSalt());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole());
            pstmt.setInt(6, user.getUserId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error updating user " + user.getUserId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer userId) throws DataAccessException {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting user " + userId + ": " + e.getMessage(), e);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        try {
            return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getString("created_at")
            );
        } catch (ValidationException e) {
            throw new SQLException("Failed mapping user row: " + e.getMessage(), e);
        }
    }
}

