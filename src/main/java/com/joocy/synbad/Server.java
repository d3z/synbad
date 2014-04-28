package com.joocy.synbad;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class Server {

    private final int port;

    public Server (final int port, final String sessionsDirName) {
        this.port = port;
        buildTheMachines(sessionsDirName);
    }

    public void start() {
        RxNetty.createHttpServer(port, (request, response) -> {
            System.out.println("Server => Request: " + request.getPath());
            response.setStatus(HttpResponseStatus.BAD_REQUEST);
            return response.writeStringAndFlush("Not yet implemented\n");
        }).startAndWait();
    }

    private void buildTheMachines(final String sessionsDirName) {
        try {
            URI sessionsDirUri = getClass().getResource(sessionsDirName).toURI();
            File sessionsDir = new File(sessionsDirUri);
            String[] sessionFiles = sessionsDir.list((dir, filename)->filename.endsWith(".session"));
            for (String sessionFileName : sessionFiles) {
                registerSessionMachine(sessionFileName);
            }
        }
        catch (URISyntaxException use) {
        }
    }

    private void registerSessionMachine(final String sessionFileName) {

    }

    public static void main(String... args) {
        new Server(9090, "/sessions").start();
    }

}
