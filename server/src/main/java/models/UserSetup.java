package models;


import org.mindrot.jbcrypt.BCrypt;

import java.io.*;

public class UserSetup {
    private static boolean userExists(String email) {
        try (BufferedReader readFromFile = new BufferedReader(new FileReader("server/src/main/resources/userCredentials.txt"))) {
            String user;
            while ((user = readFromFile.readLine()) != null) {
                String[] parts = user.split(":");
                String storedEmail = parts[0];
                if (storedEmail.equalsIgnoreCase(email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
        public static void createUser(String email, String password) {
        if (userExists(email)){
            System.out.println(email + " already exists");
            return;
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (BufferedWriter writeToFile = new BufferedWriter(new FileWriter("server/src/main/resources/userCredentials.txt", true))) {
            writeToFile.write( email + ":" + hashedPassword);
            writeToFile.newLine();
            System.out.println("New user added successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        createUser("user@gmail.com", "password123");
    }
}
