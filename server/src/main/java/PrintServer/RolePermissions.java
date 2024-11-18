package PrintServer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class RolePermissions {

    // Enum for methods
    public enum PrintServerMethods {
        START,
        STOP,
        RESTART,
        LOGIN,
        SIGNUP,
        PRINT,
        QUEUE,
        TOP_QUEUE,
        STATUS,
        READ_CONFIG,
        SET_CONFIG,
    }

    public static Map<String, List<PrintServerMethods>> getRolePermissions(String filePath) {
        // Create a new map to store roles with their allowed methods as enums
        Map<String, List<PrintServerMethods>> rolePermissions = new HashMap<>();

        // Using try-with-resources to automatically close the FileReader
        try (FileReader fileReader = new FileReader(new File(filePath))) {
            StringBuilder jsonContent = new StringBuilder();
            int ch;
            // Read the content of the file
            while ((ch = fileReader.read()) != -1) {
                jsonContent.append((char) ch);
            }

            // Parse the JSON content
            JSONObject jsonObject = new JSONObject(jsonContent.toString());

            // Loop through each role and convert method names to enums
            for (String role : jsonObject.keySet()) {
                JSONArray methodsArray = jsonObject.getJSONArray(role);
                List<PrintServerMethods> allowedMethods = new ArrayList<>();

                // Convert the list of method names to a list of PrintServerMethods enums
                for (int i = 0; i < methodsArray.length(); i++) {
                    String method = methodsArray.getString(i);
                    try {
                        allowedMethods.add(PrintServerMethods.valueOf(method));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid method: " + method + " for role: " + role);
                    }
                }

                // Add the role and its allowed methods to the result map
                rolePermissions.put(role, allowedMethods);
            }
        }catch (IOException e) {
            System.err.println("Error reading permissions file: " + e.getMessage());
        }

        return rolePermissions;
    }
}
