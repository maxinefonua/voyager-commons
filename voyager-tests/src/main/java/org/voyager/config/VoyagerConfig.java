package org.voyager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class VoyagerConfig {
    private static final Properties properties = new Properties();
    private static final Logger LOGGER = LoggerFactory.getLogger(VoyagerConfig.class);
    static {
        // 1. Load the common base file first
        loadPropertiesFile("config.properties");

        // 2. Load system env vars
        loadSystemEnvVariables();
    }

    private static void loadPropertiesFile(String filename) {
        try (InputStream input = VoyagerConfig.class.getClassLoader().getResourceAsStream(filename)) {
            if (input != null) {
                properties.load(input);
            } else {
                LOGGER.info("Warning: Profile-specific file not found: " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration from: " + filename, e);
        }
    }

    private static void loadSystemEnvVariables() {
        properties.forEach((key,value) -> {
            LOGGER.debug(String.format("properties key: '%s', value: '%s'", key, value));
            Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
            pattern.matcher((String)value)
                    .results().map(matchResult -> matchResult.group(1))
                    .forEach(variable -> {
                        String actualValue = System.getenv(variable);
                        properties.put(key,actualValue);
                        LOGGER.info(String.format("loaded system env var: '%s', value: '%s'",
                                variable, actualValue));
                    });
        });
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
