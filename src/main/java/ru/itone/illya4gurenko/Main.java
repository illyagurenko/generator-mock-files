package ru.itone.illya4gurenko;


import com.sun.net.httpserver.HttpServer;
import ru.itone.illya4gurenko.dto.ParametersRequest;
import ru.itone.illya4gurenko.file_generation.DataFakerGenerator;
import ru.itone.illya4gurenko.file_generation.DataGenerator;
import ru.itone.illya4gurenko.file_generation.FileGenerator;
import ru.itone.illya4gurenko.file_generation.TxtGenerator;
import ru.itone.illya4gurenko.handler.ParametersHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        DataGenerator dataGenerator = new DataFakerGenerator();
        FileGenerator fileGenerator = new TxtGenerator(dataGenerator);
        ParametersHandler parametersHandler = new ParametersHandler(fileGenerator);
        server.createContext("/api/parametres", parametersHandler);
        server.setExecutor(null);
        server.start();
        System.out.println("server working");



    }
}