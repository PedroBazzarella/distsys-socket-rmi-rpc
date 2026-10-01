package com.pedrobazza.distsyschat.core.ports;

import com.pedrobazza.distsyschat.core.domain.Message;

@FunctionalInterface
public interface MessageListener {
    void onMessageReceived(Message message);
}
