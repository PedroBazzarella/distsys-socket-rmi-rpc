package com.pedrobazza.distsyschat.adapters.rmi;

import com.pedrobazza.distsyschat.core.domain.Message;
import com.pedrobazza.distsyschat.core.ports.ChatServerPort;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiServerAdapter implements ChatServerPort {
    public static final String SERVICE_NAME = "ChatService";

    private Registry registry;
    private RmiRemoteServiceImpl serviceImpl;
    private volatile boolean running = false;
    private int port;

    @Override
    public synchronized void start(int port) throws Exception {
        if (running) return;
        this.port = port;

        String rmiHost = System.getenv("RMI_HOST");
        if (rmiHost != null && !rmiHost.isBlank()) {
            System.setProperty("java.rmi.server.hostname", rmiHost.trim());
        }

        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (Exception e) {
            registry = LocateRegistry.getRegistry(port);
        }

        serviceImpl = new RmiRemoteServiceImpl(0);
        registry.rebind(SERVICE_NAME, serviceImpl);
        running = true;
        System.out.println("[RmiServer] Servidor RMI iniciado na porta " + port + " com o serviço '" + SERVICE_NAME + "'");
    }

    @Override
    public synchronized void stop() throws Exception {
        if (!running) return;
        running = false;
        try {
            if (registry != null) {
                registry.unbind(SERVICE_NAME);
            }
        } catch (Exception ignored) {}

        if (serviceImpl != null) {
            serviceImpl.clearClients();
            try {
                UnicastRemoteObject.unexportObject(serviceImpl, true);
            } catch (Exception ignored) {}
        }
        System.out.println("[RmiServer] Servidor RMI parado com sucesso.");
    }

    @Override
    public void broadcast(Message message) {
        if (serviceImpl != null) {
            serviceImpl.broadcast(message);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getConnectedClientsCount() {
        return serviceImpl != null ? serviceImpl.getConnectedClientsCount() : 0;
    }
}
