package PrintServer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = PrintServer.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new FileNotFoundException("Configuration file 'config.properties' not found in resources.");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
