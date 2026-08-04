package ru.itone.illya4gurenko.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itone.illya4gurenko.security.AESCryptoService;
import ru.itone.illya4gurenko.security.AuthService;
import ru.itone.illya4gurenko.service.DataFakerGeneratorService;
import ru.itone.illya4gurenko.service.DataGenerator;
import ru.itone.illya4gurenko.service.FileGenerator;
import ru.itone.illya4gurenko.service.SimpleFileGeneratorService;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Base {

    protected static final AppConfig config = AppConfig.getInstance();
    protected static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final Logger logger = initLogger();

    private static Logger initLogger() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        String logbackPath = config.getLogbackConfigPath();

        if (logbackPath != null && !logbackPath.isBlank()) {
            Path path = Paths.get(logbackPath);
            if (Files.exists(path)) {
                try (InputStream fileConfigLogger = Files.newInputStream(path)) {
                    loggerContext.reset();
                    JoranConfigurator configurator = new JoranConfigurator();
                    configurator.setContext(loggerContext);
                    configurator.doConfigure(fileConfigLogger);
                    return LoggerFactory.getLogger(Base.class);
                } catch (Exception e) {
                    System.err.println("failed to load external logback config: " + e.getMessage());
                }
            }
        }
        return defaultInitLogger(loggerContext);
    }

    private static Logger defaultInitLogger(LoggerContext loggerContext) {
        try (InputStream resourceConfigLogger = Base.class.getResourceAsStream("/logback-default.xml")) {
            if (resourceConfigLogger != null) {
                loggerContext.reset();
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(loggerContext);
                configurator.doConfigure(resourceConfigLogger);
            }
        } catch (Exception ex) {
            System.err.println("error during Logback initialization: " + ex.getMessage());
        }
        return LoggerFactory.getLogger(Base.class);
    }


    public static AppConfig getConfig() {
        return config;
    }

    public static AESCryptoService getCryptoService() {
        return AESCryptoService.getInstance();
    }

    public static AuthService getAuthService() {
        return AuthService.getInstance();
    }

    public static DataGenerator getDataGenerator() {
        return DataFakerGeneratorService.getInstance();
    }

    public static FileGenerator getFileGenerator() {
        return SimpleFileGeneratorService.getInstance();
    }

    public void info(String message, Object... obj) { logger.info(message, obj); }
    public void info(String message) { logger.info(message); }
    public void debug(String message, Object... obj) { logger.debug(message, obj); }
    public void warn(String message, Object... obj) { logger.warn(message, obj); }
    public void error(String message, Throwable t) { logger.error(message, t); }
    public void error(String message, Object... obj) { logger.error(message, obj); }
}