package org.voyager.config;

import java.io.InputStream;
import java.util.Properties;

public class VoyagerClientConfig {
    private static final Properties properties = new Properties();
    static {
        // 1. Load the common base file first
        loadPropertiesFile("config.properties");

        // 2. Determine the active profile
        String activeProfile = System.getenv("APP_ENV");
        if (activeProfile == null || activeProfile.isEmpty()) {
            activeProfile = "local"; // default to local if not set
        }

        // 3. Load the profile-specific file, which overrides the common values
        loadPropertiesFile("config-" + activeProfile + ".properties");

        // 4. Load system env vars
        String authToken = System.getenv("voyager.auth.token");
        properties.putIfAbsent("voyager.auth.token",authToken);
    }

    private static void loadPropertiesFile(String filename) {
        try (InputStream input = VoyagerClientConfig.class.getClassLoader().getResourceAsStream(filename)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.out.println("Warning: Profile-specific file not found: " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration from: " + filename, e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
