package ru.itone.illya4gurenko;


import com.sun.net.httpserver.HttpServer;
import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.handler.ParametersHandler;

import java.net.InetSocketAddress;

public class Main extends Base {

    public static void main(String[] args) {
        new Main().startServer();
    }

    public void startServer() {
        try {
            int port = config.getServerPort();
            String endpoint = config.getServerEndpointPost();

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(endpoint, new ParametersHandler());
            server.setExecutor(null);
            server.start();

            info("server started on port {} with endpoint: {}", port, endpoint);
        } catch (Exception e) {
            error("failed to start application", e);
            System.exit(1);
        }
    }
}