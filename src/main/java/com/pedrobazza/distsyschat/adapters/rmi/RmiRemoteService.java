package com.pedrobazza.distsyschat.adapters.rmi;

import com.pedrobazza.distsyschat.core.domain.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiRemoteService extends Remote {

    interface ClientCallback extends Remote {
        void receiveMessage(Message message) throws RemoteException;
    }

    void registerClient(String username, ClientCallback callback) throws RemoteException;
    void unregisterClient(String username) throws RemoteException;
    void postMessage(Message message) throws RemoteException;
}
