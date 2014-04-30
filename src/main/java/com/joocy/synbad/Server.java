package com.joocy.synbad;

import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

public class Server {

    private final int port;

    private final Map<String, Machine> machines = new HashMap<>();
    private final Map<String, State> states = new HashMap<>();

    private final State defaultState = resp -> {
        resp.setStatus(BAD_REQUEST);
        return resp.writeStringAndFlush("No handler defined for this endpoint");
    };
    private final Machine defaultMachine = new Machine(new State[]{defaultState});

    public Server (final int port, final String sessionsDirName) {
        this.port = port;
        buildStates();
        buildTheMachines(sessionsDirName);
    }

    public void start() {
        RxNetty.createHttpServer(port, (request, response) -> {
            System.out.println("Server => Request: " + request.getPath());
            return getMachine(request).orElse(defaultMachine).execute(response);
        }).startAndWait();
    }

    private void buildStates() {
        states.put("return200", resp -> {resp.setStatus(OK);return resp.writeStringAndFlush("OK");});
        states.put("return500", resp -> {resp.setStatus(INTERNAL_SERVER_ERROR); return resp.writeStringAndFlush("BAD REQUEST");});
        states.put("return404", resp -> {resp.setStatus(NOT_FOUND);return resp.writeStringAndFlush("NOT FOUND");});
    }

    private void buildTheMachines(final String sessionsDirName) {
        try {
            URI sessionsDirUri = getClass().getResource(sessionsDirName).toURI();
            Files.walk(new File(sessionsDirUri).toPath())
                    .map(f -> f.toAbsolutePath().toString())
                    .filter(f -> f.endsWith(".machine"))
                    .forEach(this::registerMachine);
        }
        catch (IOException ioe) {
        }
        catch (URISyntaxException use) {
        }
    }

    private void registerMachine(final String machineFileName) {
        try {
            String machineFile = new String(Files.readAllBytes(new File(machineFileName).toPath()));
            String[] machineBits = machineFile.split(",");
            String machineTrigger = machineBits[0];
            String[] machineSteps = Arrays.copyOfRange(machineBits, 1, machineBits.length);
            State[] machineStates = new State[machineSteps.length];
            for (int i = 0; i < machineStates.length; i++) {
                machineStates[i] = Optional.ofNullable(states.get(machineSteps[i])).orElse(defaultState);
            }
            machines.put(machineTrigger, new Machine(machineStates));
        }
        catch (IOException ioe) {
        }
    }

    private Optional<Machine> getMachine(HttpServerRequest request) {
        return Optional.ofNullable(machines.get(request.getPath()));
    }

    public static void main(String... args) {
        new Server(9090, "/sessions").start();
    }

}
