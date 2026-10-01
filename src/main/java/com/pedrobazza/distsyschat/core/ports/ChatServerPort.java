package com.pedrobazza.distsyschat.core.ports;

import com.pedrobazza.distsyschat.core.domain.Message;

public interface ChatServerPort {
    void start(int port) throws Exception;
    void stop() throws Exception;
    void broadcast(Message message);
    boolean isRunning();
    int getConnectedClientsCount();
}
