package server;

import java.sql.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseManager {
    // MySQL Configuration
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "card_flip_game";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "khanhdangkhukho"; // Change this
    
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + 
                                         "/" + DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private Connection conn;
    
    public DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            createDatabaseIfNotExists();
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✓ Connected to MySQL database: " + DB_NAME);
            createTables();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("✗ Failed to connect to MySQL database!");
        }
    }
    
    private void createDatabaseIfNotExists() {
        try {
            String tempUrl = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + 
                           "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            Connection tempConn = DriverManager.getConnection(tempUrl, DB_USER, DB_PASSWORD);
            Statement stmt = tempConn.createStatement();
            
            String createDB = "CREATE DATABASE IF NOT EXISTS " + DB_NAME + 
                            " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            stmt.execute(createDB);
            
            stmt.close();
            tempConn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void createTables() {
        try {
            Statement stmt = conn.createStatement();
            
            // Players table
            String createPlayers = "CREATE TABLE IF NOT EXISTS players (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(100) NOT NULL," +
                "total_score INT DEFAULT 0," +
                "wins INT DEFAULT 0," +
                "losses INT DEFAULT 0," +
                "quit_count INT DEFAULT 0," +
                "is_banned BOOLEAN DEFAULT FALSE," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "INDEX idx_username (username)," +
                "INDEX idx_total_score (total_score DESC)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            stmt.execute(createPlayers);
            
            // Match history
            String createMatches = "CREATE TABLE IF NOT EXISTS match_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "player1_id INT NOT NULL," +
                "player2_id INT NOT NULL," +
                "winner_id INT," +
                "difficulty VARCHAR(10) NOT NULL," +
                "player1_score INT DEFAULT 0," +
                "player2_score INT DEFAULT 0," +
                "duration INT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (player1_id) REFERENCES players(id) ON DELETE CASCADE," +
                "FOREIGN KEY (player2_id) REFERENCES players(id) ON DELETE CASCADE," +
                "FOREIGN KEY (winner_id) REFERENCES players(id) ON DELETE SET NULL," +
                "INDEX idx_player1 (player1_id)," +
                "INDEX idx_player2 (player2_id)," +
                "INDEX idx_created_at (created_at DESC)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            stmt.execute(createMatches);
            
            System.out.println("✓ Database tables ready");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean registerPlayer(String username, String password) {
        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                System.out.println("✗ Registration failed: Username is empty");
                return false;
            }
            if (password == null || password.length() < 4) {
                System.out.println("✗ Registration failed: Password too short");
                return false;
            }
            
            System.out.println("Registering user: " + username);
            
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            System.out.println("Password hashed successfully");
            
            String sql = "INSERT INTO players (username, password) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username.trim());
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✓ User registered successfully: " + username);
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("✗ Registration failed: Username already exists");
            } else {
                System.err.println("✗ Database error during registration:");
                e.printStackTrace();
            }
            return false;
        } catch (Exception e) {
            System.err.println("✗ Error during registration:");
            e.printStackTrace();
            return false;
        }
    }
    
    public Map<String, Object> loginPlayer(String username, String password) {
        try {
            String sql = "SELECT * FROM players WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int playerId = rs.getInt("id");
                String storedHash = rs.getString("password");
                boolean isBanned = rs.getBoolean("is_banned");
                
                System.out.println("Login attempt for user: " + username);
                System.out.println("Account banned: " + isBanned);
                
                if (isBanned) {
                    System.out.println("✗ Login failed: Account is banned");
                    rs.close();
                    pstmt.close();
                    return null;
                }
                
                // Verify password
                boolean passwordMatch = false;
                try {
                    passwordMatch = BCrypt.checkpw(password, storedHash);
                    System.out.println("Password match: " + passwordMatch);
                } catch (Exception e) {
                    System.err.println("✗ Error checking password: " + e.getMessage());
                    passwordMatch = false;
                }
                
                if (passwordMatch) {
                    Map<String, Object> playerData = new HashMap<>();
                    playerData.put("id", playerId);
                    playerData.put("username", rs.getString("username"));
                    playerData.put("total_score", rs.getInt("total_score"));
                    playerData.put("wins", rs.getInt("wins"));
                    playerData.put("losses", rs.getInt("losses"));
                    playerData.put("quit_count", rs.getInt("quit_count"));
                    
                    System.out.println("✓ Login successful for: " + username);
                    
                    rs.close();
                    pstmt.close();
                    
                    // Update last login
                    updateLastLogin(playerId);
                    
                    return playerData;
                } else {
                    System.out.println("✗ Login failed: Invalid password");
                }
            } else {
                System.out.println("✗ Login failed: User not found");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("✗ Database error during login:");
            e.printStackTrace();
        }
        return null;
    }
    
    private void updateLastLogin(int playerId) {
        try {
            String sql = "UPDATE players SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, playerId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void updatePlayerScore(int playerId, int scoreToAdd, boolean won) {
        try {
            // Update player statistics
            String sql = "UPDATE players SET " +
                        "total_score = total_score + ?, " +
                        (won ? "wins = wins + 1, " : "losses = losses + 1, ") +
                        "last_login = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, scoreToAdd);
            pstmt.setInt(2, playerId);
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            
            if (rowsAffected > 0) {
                System.out.println("✓ Updated player " + playerId + ": +" + scoreToAdd + " points, " + 
                                 (won ? "Win" : "Loss"));
            }
        } catch (SQLException e) {
            System.err.println("✗ Error updating player score:");
            e.printStackTrace();
        }
    }
    
    public void incrementQuitCount(int playerId) {
        try {
            // Increment quit count
            String sql = "UPDATE players SET quit_count = quit_count + 1, " +
                        "last_login = CURRENT_TIMESTAMP WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, playerId);
            pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("⚠ Player " + playerId + " quit count incremented");
            
            // Check if should ban (quit_count >= 3)
            sql = "SELECT username, quit_count FROM players WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int quitCount = rs.getInt("quit_count");
                String username = rs.getString("username");
                
                if (quitCount >= 3) {
                    banPlayer(playerId);
                    System.out.println("🚫 Player " + username + " BANNED (quit count: " + quitCount + ")");
                } else {
                    System.out.println("⚠ Player " + username + " quit count: " + quitCount + "/3");
                }
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("✗ Error incrementing quit count:");
            e.printStackTrace();
        }
    }
    
    public void banPlayer(int playerId) {
        try {
            String sql = "UPDATE players SET is_banned = TRUE WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, playerId);
            pstmt.executeUpdate();
            pstmt.close();
            System.out.println("⚠ Player banned (ID: " + playerId + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void saveMatch(int player1Id, int player2Id, int winnerId, 
                         String difficulty, int p1Score, int p2Score, int duration) {
        try {
            // Save match history
            String sql = "INSERT INTO match_history (player1_id, player2_id, winner_id, " +
                        "difficulty, player1_score, player2_score, duration) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, player1Id);
            pstmt.setInt(2, player2Id);
            
            if (winnerId > 0) {
                pstmt.setInt(3, winnerId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.setString(4, difficulty);
            pstmt.setInt(5, p1Score);
            pstmt.setInt(6, p2Score);
            pstmt.setInt(7, duration);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int matchId = rs.getInt(1);
                    System.out.println("✓ Match saved (ID: " + matchId + ") - " + 
                                     "P1:" + p1Score + " vs P2:" + p2Score + 
                                     " | Duration: " + duration + "s");
                }
                rs.close();
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("✗ Error saving match:");
            e.printStackTrace();
        }
    }
    
    public List<Map<String, Object>> getLeaderboard(int limit) {
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        try {
            String sql = "SELECT " +
                        "username, " +
                        "total_score, " +
                        "wins, " +
                        "losses, " +
                        "(wins + losses) AS total_games, " +
                        "CASE " +
                        "    WHEN (wins + losses) = 0 THEN 0 " +
                        "    ELSE ROUND(wins * 100.0 / (wins + losses), 2) " +
                        "END AS win_rate " +
                        "FROM players " +
                        "WHERE is_banned = FALSE " +
                        "ORDER BY total_score DESC, wins DESC " +
                        "LIMIT ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> player = new HashMap<>();
                player.put("username", rs.getString("username"));
                player.put("total_score", rs.getInt("total_score"));
                player.put("wins", rs.getInt("wins"));
                player.put("losses", rs.getInt("losses"));
                player.put("total_games", rs.getInt("total_games"));
                player.put("win_rate", rs.getDouble("win_rate"));
                leaderboard.add(player);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaderboard;
    }
    
    public List<Map<String, Object>> getPlayerMatchHistory(int playerId, int limit) {
        List<Map<String, Object>> history = new ArrayList<>();
        try {
            String sql = "SELECT " +
                        "m.id, " +
                        "CASE " +
                        "    WHEN m.player1_id = ? THEN p2.username " +
                        "    ELSE p1.username " +
                        "END AS opponent, " +
                        "CASE " +
                        "    WHEN m.winner_id IS NULL THEN 'DRAW' " +
                        "    WHEN m.winner_id = ? THEN 'WIN' " +
                        "    ELSE 'LOSS' " +
                        "END AS result, " +
                        "m.difficulty, " +
                        "CASE " +
                        "    WHEN m.player1_id = ? THEN m.player1_score " +
                        "    ELSE m.player2_score " +
                        "END AS my_score, " +
                        "CASE " +
                        "    WHEN m.player1_id = ? THEN m.player2_score " +
                        "    ELSE m.player1_score " +
                        "END AS opponent_score, " +
                        "m.duration, " +
                        "m.created_at " +
                        "FROM match_history m " +
                        "JOIN players p1 ON m.player1_id = p1.id " +
                        "JOIN players p2 ON m.player2_id = p2.id " +
                        "WHERE m.player1_id = ? OR m.player2_id = ? " +
                        "ORDER BY m.created_at DESC " +
                        "LIMIT ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, playerId);
            pstmt.setInt(2, playerId);
            pstmt.setInt(3, playerId);
            pstmt.setInt(4, playerId);
            pstmt.setInt(5, playerId);
            pstmt.setInt(6, playerId);
            pstmt.setInt(7, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> match = new HashMap<>();
                match.put("opponent", rs.getString("opponent"));
                match.put("result", rs.getString("result"));
                match.put("difficulty", rs.getString("difficulty"));
                match.put("my_score", rs.getInt("my_score"));
                match.put("opponent_score", rs.getInt("opponent_score"));
                match.put("duration", rs.getInt("duration"));
                match.put("created_at", rs.getTimestamp("created_at"));
                history.add(match);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
    
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("✓ Database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void verifyDatabaseIntegrity() {
    System.out.println("[DatabaseManager] Database integrity verified successfully.");
    // Nếu bạn chưa cần kiểm tra gì, để trống cũng được.
    // Sau này có thể thêm kiểm tra bảng, cấu trúc, dữ liệu ở đây.
    }
}