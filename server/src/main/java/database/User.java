package database;

import org.json.JSONObject;

public class User {
    private final String email;
    private final String hashedPassword;
    private String permissionsKey;

    public User(String email, String hashedPassword, String permissionsKey) {
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.permissionsKey = permissionsKey;
    }

    public User(String email, String hashedPassword) {
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.permissionsKey = email;
    }
    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getPermissionsKey() {
        return permissionsKey;
    }

    public void setPermissionsKey(String permissionsKey) {
        this.permissionsKey = permissionsKey;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("hashedPassword", hashedPassword);
        jsonObject.put("permissionsKey", permissionsKey);
        return jsonObject;
    }

    public static User fromJson(JSONObject jsonObject) {
        String email = jsonObject.getString("email");
        String hashedPassword = jsonObject.getString("hashedPassword");
        String permissionsKey = jsonObject.optString("permissionsKey", email); // Default to email if not present
        return new User(email, hashedPassword, permissionsKey);
    }
}
