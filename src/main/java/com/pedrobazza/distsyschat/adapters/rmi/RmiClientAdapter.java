package com.pedrobazza.distsyschat.adapters.rmi;

import com.pedrobazza.distsyschat.core.domain.Message;
import com.pedrobazza.distsyschat.core.ports.ChatClientPort;
import com.pedrobazza.distsyschat.core.ports.MessageListener;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiClientAdapter implements ChatClientPort {
    private RmiRemoteService remoteService;
    private RmiRemoteService.ClientCallback callback;
    private String username;
    private volatile boolean connected = false;

    @Override
    public synchronized void connect(String host, int port, String username, MessageListener listener) throws Exception {
        if (connected) {
            throw new IllegalStateException("Cliente já está conectado.");
        }
        this.username = username;

        Registry registry = LocateRegistry.getRegistry(host, port);
        this.remoteService = (RmiRemoteService) registry.lookup(RmiServerAdapter.SERVICE_NAME);

        this.callback = (RmiRemoteService.ClientCallback) UnicastRemoteObject.exportObject(
                (RmiRemoteService.ClientCallback) listener::onMessageReceived, 0);

        this.remoteService.registerClient(username, this.callback);
        this.connected = true;
        System.out.println("[RmiClient] Conectado ao servidor RMI com sucesso.");
    }

    @Override
    public synchronized void sendMessage(String content) throws Exception {
        if (!connected || remoteService == null) {
            throw new IllegalStateException("Cliente não está conectado.");
        }
        Message message = new Message(username, content);
        remoteService.postMessage(message);
    }

    @Override
    public synchronized void disconnect() throws Exception {
        if (!connected) return;
        connected = false;
        try {
            if (remoteService != null && username != null) {
                remoteService.unregisterClient(username);
            }
        } catch (Exception ignored) {}

        if (callback != null) {
            try {
                UnicastRemoteObject.unexportObject(callback, true);
            } catch (Exception ignored) {}
        }
        System.out.println("[RmiClient] Desconectado do servidor RMI.");
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
