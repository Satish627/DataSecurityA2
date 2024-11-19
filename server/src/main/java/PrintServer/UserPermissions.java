package PrintServer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class UserPermissions {

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
        SET_CONFIG
    }

    public static Map<String, List<PrintServerMethods>> getUserPermissions(String filePath) {
        Map<String, List<PrintServerMethods>> userPermissions = new HashMap<>();

        try (FileReader fileReader = new FileReader(new File(filePath))) {
            StringBuilder jsonContent = new StringBuilder();
            int ch;

            // Read the content of the file
            while ((ch = fileReader.read()) != -1) {
                jsonContent.append((char) ch);
            }

            // Parse the JSON content
            JSONObject jsonObject = new JSONObject(jsonContent.toString());

            // Loop through each user and convert operation names to enums
            for (String user : jsonObject.keySet()) {
                JSONArray methodsArray = jsonObject.getJSONArray(user);
                List<PrintServerMethods> allowedMethods = new ArrayList<>();

                // Convert the list of method names to PrintServerMethods enums
                for (int i = 0; i < methodsArray.length(); i++) {
                    String method = methodsArray.getString(i);
                    try {
                        allowedMethods.add(PrintServerMethods.valueOf(method.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid method: " + method + " for user: " + user);
                    }
                }

                // Add the user and their allowed methods to the result map
                userPermissions.put(user, allowedMethods);
            }
        } catch (IOException e) {
            System.err.println("Error reading permissions file: " + e.getMessage());
        }

        return userPermissions;
    }
}
