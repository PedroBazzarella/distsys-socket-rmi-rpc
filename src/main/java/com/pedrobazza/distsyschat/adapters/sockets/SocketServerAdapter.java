package com.pedrobazza.distsyschat.adapters.sockets;

import com.pedrobazza.distsyschat.core.domain.Message;
import com.pedrobazza.distsyschat.core.ports.ChatServerPort;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServerAdapter implements ChatServerPort {
    private ServerSocket serverSocket;
    private final Set<SocketClientHandler> clients = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private ExecutorService threadPool;
    private volatile boolean running = false;

    @Override
    public synchronized void start(int port) throws IOException {
        if (running) return;
        this.serverSocket = new ServerSocket(port);
        this.running = true;
        this.threadPool = Executors.newCachedThreadPool();

        System.out.println("[SocketServer] Servidor Socket iniciado na porta " + port);

        threadPool.execute(() -> {
            while (running && !serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    SocketClientHandler handler = new SocketClientHandler(clientSocket, this);
                    threadPool.execute(handler);
                } catch (IOException e) {
                    if (!running) break;
                    System.err.println("[SocketServer] Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public synchronized void stop() throws IOException {
        if (!running) return;
        running = false;
        for (SocketClientHandler client : clients) {
            client.close();
        }
        clients.clear();
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
        System.out.println("[SocketServer] Servidor parado.");
    }

    @Override
    public void broadcast(Message message) {
        System.out.println("[SocketServer] Broadcast: " + message.formatDisplay());
        for (SocketClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getConnectedClientsCount() {
        return clients.size();
    }

    public void registerClient(SocketClientHandler handler) {
        clients.add(handler);
        System.out.println("[SocketServer] Cliente registrado: " + handler.getUsername() + " (Total: " + clients.size() + ")");
    }

    public void unregisterClient(SocketClientHandler handler) {
        clients.remove(handler);
        System.out.println("[SocketServer] Cliente desconectado. Total restante: " + clients.size());
    }
}
