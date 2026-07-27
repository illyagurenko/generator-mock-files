package ru.itone.illya4gurenko.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Properties;

public class AppConfig {
    private static final Properties PROPERTIES = new Properties();

    static{
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties not found");
            }
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new RuntimeException("error load configuration", e);
        }

    }
    public static int getServerPort() {
        return Integer.parseInt(PROPERTIES.getProperty("server.port", "8080"));
    }

    public static String getServerEndpoint() {
        return PROPERTIES.getProperty("server.endpoint", "/api/parametres");
    }

    public static Path getFileOut() {
        return Path.of(PROPERTIES.getProperty("file.output-dir", "./generated"));
    }

    public static Charset getFileCharset() {
        return Charset.forName(PROPERTIES.getProperty("file.charset", "UTF-8"));
    }

    public static String getFakerLocale() {
        return PROPERTIES.getProperty("faker.locale", "ru");
    }
}
