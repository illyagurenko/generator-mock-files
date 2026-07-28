package ru.itone.illya4gurenko.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.slf4j.LoggerFactory;
import ru.itone.illya4gurenko.security.AESCryptoService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class AppConfig {
    private static final Properties PROPERTIES = new Properties();
    private static AESCryptoService cryptoService;

    static {
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties not found");
            }
            PROPERTIES.load(input);

            String logPath = PROPERTIES.getProperty("log.file-path", "./logs/app.log");
            System.setProperty("dynamic.log.path", logPath);

            initLogback();

            initCryptoService();

        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    private static void initLogback() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        String logbackConfigPath = System.getProperty("project.logger",
                PROPERTIES.getProperty("logback.config.path"));

        if (logbackConfigPath != null && !logbackConfigPath.isBlank()) {
            Path path = Paths.get(logbackConfigPath);
            if (Files.exists(path)) {
                try (InputStream fileConfigLogger = Files.newInputStream(path)) {
                    loggerContext.reset();
                    JoranConfigurator configurator = new JoranConfigurator();
                    configurator.setContext(loggerContext);
                    configurator.doConfigure(fileConfigLogger);
                    return;
                } catch (Exception e) {
                    System.err.println("Failed to load external logback config, falling back to default: " + e.getMessage());
                }
            }
        }
        try (InputStream resourceConfigLogger = AppConfig.class.getResourceAsStream("/logback-default.xml")) {
            if (resourceConfigLogger != null) {
                loggerContext.reset();
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(loggerContext);
                configurator.doConfigure(resourceConfigLogger);
            }
        } catch (Exception ex) {
            System.err.println("Critical: Error during Logback initialization from resources: " + ex.getMessage());
        }
    }

    private static void initCryptoService() {
        String keyPath = System.getProperty("gpb.dppt.cld.midnight.key",
                PROPERTIES.getProperty("crypto.key.path"));
        cryptoService = new AESCryptoService(keyPath);
    }

    public static AESCryptoService getCryptoService() {
        return cryptoService;
    }

    public static int getServerPort() {
        return Integer.parseInt(PROPERTIES.getProperty("server.port", "8080"));
    }

    public static String getServerEndpointPost() {
        return PROPERTIES.getProperty("server.endpoint.post", "/api/parametres");
    }

    public static Path getFileOut() {
        return Path.of(PROPERTIES.getProperty("file.out", "./generated"));
    }

    public static Charset getFileCharset() {
        return Charset.forName(PROPERTIES.getProperty("file.charset", "UTF-8"));
    }

    public static String getFakerLocale() {
        return PROPERTIES.getProperty("faker.locale", "ru");
    }
}