package database;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Database {
    private static final String USERS_FILE = "server/src/main/resources/users.json";

    public static Optional<User> addUser(String email, String password){
        List<User> users = getAllUsers();
        boolean userExists = users.stream().anyMatch(user -> user.getEmail().equals(email));
        if (userExists) {
            return Optional.empty();
        }

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(email, hashed);
        users.add(user);
        saveUsers(users);
        return Optional.of(user);
    }

    public static Optional<User> verifyUser(String email, String password){
        List<User> users = getAllUsers();
        User user = users.stream().filter(u -> u.getEmail().equals(email)).findFirst().orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        boolean isCorrectPassword = BCrypt.checkpw(password, user.getHashedPassword());
        if (!isCorrectPassword) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    private static List<User> getAllUsers(){
        List<User> users = new ArrayList<>();

        try {
            // Read JSON file
            System.out.println("Starting to read");
            String content = new String(Files.readAllBytes(Paths.get(USERS_FILE)));

            System.out.println("Content: " + content);
            JSONArray jsonArray = new JSONArray(content);

            // Convert each JSON object to a User
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                User user = User.fromJson(jsonObject);
                users.add(user);
            }
        } catch (IOException e) {
            System.err.println("Error reading users.json: " + e.getMessage());
        }

        return users;
    }

    private static void saveUsers(List<User> users) {
        JSONArray jsonArray = new JSONArray();

        // Convert each User to JSON and add to JSONArray
        for (User user : users) {
            jsonArray.put(user.toJson());
        }

        try {
            // Write JSONArray to file
            Files.write(Paths.get(USERS_FILE), jsonArray.toString(4).getBytes());
        } catch (IOException e) {
            System.err.println("Error saving users.json: " + e.getMessage());
        }
    }
}
