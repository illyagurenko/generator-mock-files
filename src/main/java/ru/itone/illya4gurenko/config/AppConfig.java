package ru.itone.illya4gurenko.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Синглтон-класс для управления конфигурацией приложения.
 * Загружает настройки из ресурсов {@code application.properties}.
 */
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
            String logPath = getProperty("log.file-path", "./logs/app.log");
            System.setProperty("dynamic.log.path", logPath);
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
        String outPath = System.getProperty("file.out", getProperty("file.out", "./generated"));
        return Path.of(outPath);
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

    public int getPercentageInvalid() {
        try {
            int prob = Integer.parseInt(getProperty("generator.file.percentage.invalid", "0"));
            return Math.max(0, Math.min(100, prob));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isSendChunkedEnabled() {
        return Boolean.parseBoolean(getProperty("file.send.chunked.enabled", "false"));
    }

    public boolean isSendMultipartEnabled() {
        return Boolean.parseBoolean(getProperty("file.send.multipart.enabled", "false"));
    }

    public boolean isSendGrpcEnabled() {
        return Boolean.parseBoolean(getProperty("file.send.grpc.enabled", "false"));
    }

    public String getGrpcReceiverUrl() {
        return getProperty("grpc.receiver.url", "localhost:9090");
    }

    public String getReceiverUrl() {
        return getProperty("receiver.url", "http://localhost:8082/api/upload/chunked");
    }
}