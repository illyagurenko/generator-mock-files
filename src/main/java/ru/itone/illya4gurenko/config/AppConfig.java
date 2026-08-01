package ru.itone.illya4gurenko.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Properties;

public class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();
    private final Properties properties = new Properties();

    private AppConfig() {
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("application.properties not found in classpath");
            }
        } catch (IOException e) {
            throw new RuntimeException("error loading application.properties", e);
        }
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }


    public int getServerPort() {
        return Integer.parseInt(getProperty("server.port", "8080"));
    }

    public String getServerEndpointPost() {
        return getProperty("server.endpoint.post", "/api/parametres");
    }

    public Path getFileOut() {
        return Path.of(getProperty("file.out", "./generated"));
    }

    public Charset getFileCharset() {
        return Charset.forName(getProperty("file.charset", "UTF-8"));
    }

    public String getFakerLocale() {
        return getProperty("faker.locale", "ru");
    }

    public String getCryptoKeyPath() {
        return System.getProperty("crypto.key", getProperty("crypto.key.path", ""));
    }

    public String getLogbackConfigPath() {
        return System.getProperty("project.logger", getProperty("logback.config.path", ""));
    }
}