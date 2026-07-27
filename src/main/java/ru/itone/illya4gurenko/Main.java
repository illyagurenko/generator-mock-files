package ru.itone.illya4gurenko;


import com.sun.net.httpserver.HttpServer;
import ru.itone.illya4gurenko.config.AppConfig;
import ru.itone.illya4gurenko.file_generation.DataFakerGenerator;
import ru.itone.illya4gurenko.file_generation.DataGenerator;
import ru.itone.illya4gurenko.file_generation.FileGenerator;
import ru.itone.illya4gurenko.file_generation.TxtGenerator;
import ru.itone.illya4gurenko.handler.ParametersHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = AppConfig.getServerPort();
        String endpoint = AppConfig.getServerEndpoint();

        DataGenerator dataGenerator = new DataFakerGenerator(AppConfig.getFakerLocale());
        FileGenerator fileGenerator = new TxtGenerator(dataGenerator, AppConfig.getFileOut(), AppConfig.getFileCharset());
        ParametersHandler parametersHandler = new ParametersHandler(fileGenerator);


        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(endpoint, parametersHandler);
        server.setExecutor(null);
        server.start();
        System.out.println("server working");



    }
}