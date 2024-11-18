package database;

import org.json.JSONObject;

public class User {
    private final String email;
    private final String hashedPassword;

    private final Role role;

    public User(String email, String hashedPassword, String role) {
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.role = Role.valueOf(role);
    }

    public User(String email, String hashedPassword) {
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.role = Role.ORDINARY;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public Role getRole() {
        return role;
    }

    // Convert User object to JSONObject
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("hashedPassword", hashedPassword);
        jsonObject.put("role", role.toString());
        return jsonObject;
    }

    public static User fromJson(JSONObject jsonObject) {
        String email = jsonObject.getString("email");
        String hashedPassword = jsonObject.getString("hashedPassword");
        String role = jsonObject.getString("role");
        return new User(email, hashedPassword, role);
    }

    public enum Role{
        MANAGER,
        JANITOR,
        POWER,
        ORDINARY
    }
}
