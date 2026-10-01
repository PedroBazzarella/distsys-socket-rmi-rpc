package com.pedrobazza.distsyschat.adapters.sockets;

import com.pedrobazza.distsyschat.core.domain.Message;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class SocketClientHandler implements Runnable {
    private final Socket socket;
    private final SocketServerAdapter server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private volatile boolean running = true;

    public SocketClientHandler(Socket socket, SocketServerAdapter server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            Object firstObj = in.readObject();
            if (firstObj instanceof Message initialMsg) {
                this.username = initialMsg.getSender();
                server.registerClient(this);
                server.broadcast(new Message("SERVER", this.username + " entrou no chat."));
            }

            while (running && !socket.isClosed()) {
                Object obj = in.readObject();
                if (obj instanceof Message message) {
                    server.broadcast(message);
                }
            }
        } catch (EOFException | SocketException e) {
            // Desconexão normal do cliente
        } catch (Exception e) {
            System.err.println("Erro na conexão com cliente " + username + ": " + e.getMessage());
        } finally {
            close();
        }
    }

    public synchronized void sendMessage(Message message) {
        if (!running || out == null) return;
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            close();
        }
    }

    public synchronized void close() {
        if (!running) return;
        running = false;
        server.unregisterClient(this);
        if (username != null) {
            server.broadcast(new Message("SERVER", username + " saiu do chat."));
        }
        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}
        try {
            if (out != null) out.close();
        } catch (IOException ignored) {}
        try {
            if (!socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public String getUsername() {
        return username;
    }
}
