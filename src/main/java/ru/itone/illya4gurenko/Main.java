package ru.itone.illya4gurenko;


import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itone.illya4gurenko.config.AppConfig;
import ru.itone.illya4gurenko.file_generation.DataFakerGeneratorService;
import ru.itone.illya4gurenko.file_generation.DataGenerator;
import ru.itone.illya4gurenko.file_generation.FileGenerator;
import ru.itone.illya4gurenko.file_generation.SimpleFileGeneratorService;
import ru.itone.illya4gurenko.handler.ParametersHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    private static Logger log;

    public static void main(String[] args) throws IOException {
        try {
            int port = AppConfig.getServerPort();
            String endpoint = AppConfig.getServerEndpointPost();

            log = LoggerFactory.getLogger(Main.class);
            log.info("configure app");

            DataGenerator dataGenerator = new DataFakerGeneratorService(AppConfig.getFakerLocale());
            FileGenerator fileGenerator = new SimpleFileGeneratorService(dataGenerator, AppConfig.getFileOut(), AppConfig.getFileCharset());
            ParametersHandler parametersHandler = new ParametersHandler(fileGenerator);

            log.info("init httpserver on port {}", port);
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext(endpoint, parametersHandler);
            server.setExecutor(null);

            server.start();

            log.info("httpserver running and listening endpoint: http://localhost:{}{}", port, endpoint);

        } catch (IOException e) {
            if (log != null) {
                log.error("io error", e);
            } else {
                e.printStackTrace();
            }
            System.exit(1);
        } catch (Exception e) {
            if (log != null) {
                log.error("error statrt app", e);
            } else {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}