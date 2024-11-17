package database;

import org.json.JSONObject;

public class User {
    private final String email;
    private final String hashedPassword;

    // Constructor
    public User(String email, String hashedPassword) {
        this.email = email;
        this.hashedPassword = hashedPassword;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    // Convert User object to JSONObject
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("hashedPassword", hashedPassword);
        return jsonObject;
    }

    // Static method to create a User from JSONObject
    public static User fromJson(JSONObject jsonObject) {
        String email = jsonObject.getString("email");
        String hashedPassword = jsonObject.getString("hashedPassword");
        return new User(email, hashedPassword);
    }
}
