package ru.itone.illya4gurenko.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Синглтон-класс для управления конфигурацией приложения.
 * Загружает настройки из ресурсов {@code application.properties}.
 */
public class AppConfig {

    private static final AppConfig INSTANCE = new AppConfig();
    private final Properties properties = new Properties();

    private AppConfig() {
        //java -Dgenerator.file.property=./config/application.properties \
        //     -Dgenerator.file.logback=./config/file-logback.xml \
        //     -jar build/libs/generator_mock_file-1.0-all.jar
        String externalPropPath = System.getProperty("generator.file.property");
        boolean loadedExternal = false;

        if (externalPropPath != null && !externalPropPath.isBlank()) {
            Path path = Paths.get(externalPropPath);
            if (Files.exists(path)) {
                try (InputStream input = Files.newInputStream(path)) {
                    properties.load(input);
                    loadedExternal = true;
                    System.out.println("Loaded external application properties from: " + path.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Failed to load external properties from: " + externalPropPath);
                }
            }
        }

        if (!loadedExternal) {
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

        String logPath = getProperty("generator.files.log.file-path", "./logs/app.log");
        System.setProperty("dynamic.log.path", logPath);
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
        return Integer.parseInt(getProperty("generator.files.server.port", "8080"));
    }

    public String getServerEndpointPost() {
        return getProperty("generator.files.server.endpoint.post", "/api/parametres");
    }

    public Path getFileOut() {
        String outPath = System.getProperty("file.out", getProperty("generator.files.file.out", "./generated"));
        return Path.of(outPath);
    }

    public Charset getFileCharset() {
        return Charset.forName(getProperty("generator.files.file.charset", "UTF-8"));
    }

    public String getFakerLocale() {
        return getProperty("generator.files.faker.locale", "ru");
    }

    public String getCryptoKeyPath() {
        return System.getProperty("crypto.key", getProperty("generator.files.crypto.key.path", "./config/secret.key"));
    }

    public String getLogbackConfigPath() {
        return System.getProperty("generator.file.logback", getProperty("generator.files.logback.config.path", "./config/file-logback.xml"));
    }

    public int getPercentageInvalid() {
        try {
            int prob = Integer.parseInt(getProperty("generator.files.percentage.invalid", "0"));
            return Math.max(0, Math.min(100, prob));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isSendChunkedEnabled() {
        return Boolean.parseBoolean(getProperty("generator.files.file.send.chunked.enabled", "false"));
    }

    public boolean isSendMultipartEnabled() {
        return Boolean.parseBoolean(getProperty("generator.files.file.send.multipart.enabled", "false"));
    }

    public boolean isSendGrpcEnabled() {
        return Boolean.parseBoolean(getProperty("generator.files.file.send.grpc.enabled", "false"));
    }


    public String getReceiverUrl() {
        return getProperty("generator.files.receiver.url", "http://localhost:8082/api/upload/chunked");
    }

    public String getGrpcReceiverUrl() {
        return getProperty("generator.files.grpc.receiver.url", "localhost:9090");
    }
}