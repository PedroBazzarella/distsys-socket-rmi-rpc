package com.pedrobazza.distsyschat.adapters.sockets;

import com.pedrobazza.distsyschat.core.domain.Message;
import com.pedrobazza.distsyschat.core.ports.ChatClientPort;
import com.pedrobazza.distsyschat.core.ports.MessageListener;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class SocketClientAdapter implements ChatClientPort {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private Thread listenerThread;
    private volatile boolean connected = false;

    @Override
    public synchronized void connect(String host, int port, String username, MessageListener listener) throws IOException {
        if (connected) {
            throw new IllegalStateException("Cliente já está conectado.");
        }
        this.username = username;
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
        this.connected = true;

        // Envia mensagem de apresentação para registrar o nome de usuário no servidor
        out.writeObject(new Message(username, "login"));
        out.flush();
        out.reset();

        listenerThread = new Thread(() -> {
            try {
                while (connected && !socket.isClosed()) {
                    Object obj = in.readObject();
                    if (obj instanceof Message message) {
                        listener.onMessageReceived(message);
                    }
                }
            } catch (EOFException | SocketException e) {
                // Desconexão normal ou socket fechado
            } catch (Exception e) {
                if (connected) {
                    System.err.println("[SocketClient] Erro ao receber mensagem: " + e.getMessage());
                }
            } finally {
                try {
                    disconnect();
                } catch (Exception ignored) {}
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    @Override
    public synchronized void sendMessage(String content) throws IOException {
        if (!connected || socket == null || socket.isClosed()) {
            throw new IllegalStateException("Cliente não está conectado.");
        }
        Message message = new Message(username, content);
        out.writeObject(message);
        out.flush();
        out.reset();
    }

    @Override
    public synchronized void disconnect() throws IOException {
        if (!connected) return;
        connected = false;
        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}
        try {
            if (out != null) out.close();
        } catch (IOException ignored) {}
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        if (listenerThread != null && listenerThread.isAlive()) {
            listenerThread.interrupt();
        }
        System.out.println("[SocketClient] Desconectado com sucesso.");
    }

    @Override
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    @Override
    public String getUsername() {
        return username;
    }
}
