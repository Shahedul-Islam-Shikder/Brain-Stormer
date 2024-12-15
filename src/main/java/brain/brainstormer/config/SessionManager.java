package brain.brainstormer.config;


public class SessionManager {
    private static SessionManager instance;
    private String userId;
    private String username;
    private String email;

    // Private constructor to prevent external instantiation
    private SessionManager() {}

    // Static method to get the instance (singleton pattern)
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Getter and Setter for userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getter and Setter for username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getter and Setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Optional: a method to clear session data when the user logs out
    public void clearSession() {
        userId = null;
        username = null;
        email = null;
    }
}
