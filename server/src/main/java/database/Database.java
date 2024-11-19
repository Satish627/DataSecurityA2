package database;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.security.SecureRandom;

public class Database {
    private static final String USERS_FILE = "server/src/main/resources/users.json";
    private static final SecretKey SECRET_KEY = generateKey();
    private static final byte[] IV = generateIV();

    // Method to generate AES key (256-bit)
    private static SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // 256-bit key size
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Error generating AES key", e);
        }
    }

    // Method to generate AES IV (16 bytes for AES)
    private static byte[] generateIV() {
        byte[] iv = new byte[16]; // AES block size is 16 bytes
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    // Encrypt a file
    private static void encryptFile(String content) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, ivSpec);

        byte[] encryptedBytes = cipher.doFinal(content.getBytes());
        Files.write(Paths.get(Database.USERS_FILE), encryptedBytes);
    }

    // Decrypt a file
    private static String decryptFile() throws Exception {
        byte[] encryptedBytes = Files.readAllBytes(Paths.get(Database.USERS_FILE));

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    public static Optional<User> addUser(String email, String password) {
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

    public static Optional<User> verifyUser(String email, String password) {
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

    private static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try {
            String content = decryptFile();
            JSONArray jsonArray = new JSONArray(content);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                User user = User.fromJson(jsonObject);
                users.add(user);
            }
        } catch (Exception e) {
            System.err.println("Error reading or decrypting users.json: " + e.getMessage());
        }

        return users;
    }

    private static void saveUsers(List<User> users) {
        JSONArray jsonArray = new JSONArray();

        for (User user : users) {
            jsonArray.put(user.toJson());
        }

        try {
            String content = jsonArray.toString(4);
            encryptFile(content);
        } catch (Exception e) {
            System.err.println("Error encrypting and saving users.json: " + e.getMessage());
        }
    }
}
