package com.pedrobazza.distsyschat.core.ports;

public interface ChatClientPort {
    void connect(String host, int port, String username, MessageListener listener) throws Exception;
    void sendMessage(String content) throws Exception;
    void disconnect() throws Exception;
    boolean isConnected();
    String getUsername();
}
