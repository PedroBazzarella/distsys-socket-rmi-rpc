package com.pedrobazza.distsyschat.adapters.grpc;

import com.pedrobazza.distsyschat.core.domain.Message;
import com.pedrobazza.distsyschat.core.ports.ChatServerPort;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GrpcServerAdapter implements ChatServerPort {
    private Server server;
    private final ChatServiceImpl chatService = new ChatServiceImpl();
    private volatile boolean running = false;

    @Override
    public synchronized void start(int port) throws IOException {
        if (running) return;
        this.server = ServerBuilder.forPort(port)
                .addService(chatService)
                .build()
                .start();
        this.running = true;
        System.out.println("[GrpcServer] Servidor gRPC iniciado na porta " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                GrpcServerAdapter.this.stop();
            } catch (Exception ignored) {}
        }));
    }

    @Override
    public synchronized void stop() throws Exception {
        if (!running) return;
        running = false;
        chatService.clearClients();
        if (server != null) {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
        System.out.println("[GrpcServer] Servidor gRPC parado com sucesso.");
    }

    @Override
    public void broadcast(Message message) {
        chatService.broadcast(message);
    }

    @Override
    public boolean isRunning() {
        return running && server != null && !server.isTerminated();
    }

    @Override
    public int getConnectedClientsCount() {
        return chatService.getConnectedClientsCount();
    }
}
