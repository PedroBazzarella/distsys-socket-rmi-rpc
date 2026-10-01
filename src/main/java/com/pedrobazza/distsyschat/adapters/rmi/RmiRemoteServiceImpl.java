package com.pedrobazza.distsyschat.adapters.rmi;

import com.pedrobazza.distsyschat.core.domain.Message;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RmiRemoteServiceImpl extends UnicastRemoteObject implements RmiRemoteService {
    private static final long serialVersionUID = 1L;

    private final Map<String, ClientCallback> clients = new ConcurrentHashMap<>();

    public RmiRemoteServiceImpl() throws RemoteException {
        super();
    }

    public RmiRemoteServiceImpl(int port) throws RemoteException {
        super(port);
    }

    @Override
    public synchronized void registerClient(String username, ClientCallback callback) throws RemoteException {
        clients.put(username, callback);
        System.out.println("[RmiServer] Cliente registrado: " + username + " (Total: " + clients.size() + ")");
        broadcast(new Message("SERVER", username + " entrou no chat."));
    }

    @Override
    public synchronized void unregisterClient(String username) throws RemoteException {
        if (clients.remove(username) != null) {
            System.out.println("[RmiServer] Cliente desconectado: " + username + " (Restantes: " + clients.size() + ")");
            broadcast(new Message("SERVER", username + " saiu do chat."));
        }
    }

    @Override
    public void postMessage(Message message) throws RemoteException {
        broadcast(message);
    }

    public void broadcast(Message message) {
        System.out.println("[RmiServer] Broadcast: " + message.formatDisplay());
        clients.forEach((user, callback) -> {
            try {
                callback.receiveMessage(message);
            } catch (RemoteException e) {
                System.err.println("[RmiServer] Falha ao entregar mensagem para " + user + ", removendo cliente.");
                clients.remove(user);
            }
        });
    }

    public int getConnectedClientsCount() {
        return clients.size();
    }

    public void clearClients() {
        clients.clear();
    }
}
