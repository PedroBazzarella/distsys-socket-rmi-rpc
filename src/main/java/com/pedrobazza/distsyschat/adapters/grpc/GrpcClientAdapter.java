package com.pedrobazza.distsyschat.adapters.grpc;

import com.pedrobazza.distsyschat.core.domain.Message;
import com.pedrobazza.distsyschat.core.ports.ChatClientPort;
import com.pedrobazza.distsyschat.core.ports.MessageListener;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class GrpcClientAdapter implements ChatClientPort {
    private ManagedChannel channel;
    private ChatServiceGrpc.ChatServiceBlockingStub blockingStub;
    private ChatServiceGrpc.ChatServiceStub asyncStub;
    private String username;
    private volatile boolean connected = false;

    @Override
    public synchronized void connect(String host, int port, String username, MessageListener listener) {
        if (connected) {
            throw new IllegalStateException("Cliente já está conectado.");
        }
        this.username = username;
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        this.blockingStub = ChatServiceGrpc.newBlockingStub(channel);
        this.asyncStub = ChatServiceGrpc.newStub(channel);

        ConnectRequest request = ConnectRequest.newBuilder()
                .setUsername(username)
                .build();

        asyncStub.streamMessages(request, new StreamObserver<ChatMessage>() {
            @Override
            public void onNext(ChatMessage chatMessage) {
                Message domainMessage = new Message(
                        chatMessage.getSender(),
                        chatMessage.getContent(),
                        chatMessage.getTimestamp(),
                        chatMessage.getRecipient()
                );
                listener.onMessageReceived(domainMessage);
            }

            @Override
            public void onError(Throwable throwable) {
                if (connected) {
                    System.err.println("[GrpcClient] Erro no fluxo de mensagens: " + throwable.getMessage());
                }
                connected = false;
            }

            @Override
            public void onCompleted() {
                connected = false;
                System.out.println("[GrpcClient] Fluxo de mensagens encerrado pelo servidor.");
            }
        });

        this.connected = true;
        System.out.println("[GrpcClient] Conectado ao servidor gRPC.");
    }

    @Override
    public synchronized void sendMessage(String content) {
        if (!connected || blockingStub == null) {
            throw new IllegalStateException("Cliente não está conectado.");
        }
        ChatMessage msg = ChatMessage.newBuilder()
                .setSender(username)
                .setContent(content)
                .setTimestamp(System.currentTimeMillis())
                .setRecipient("ALL")
                .build();

        blockingStub.sendMessage(msg);
    }

    @Override
    public synchronized void disconnect() {
        if (!connected) return;
        connected = false;
        try {
            if (blockingStub != null && username != null) {
                blockingStub.disconnect(DisconnectRequest.newBuilder().setUsername(username).build());
            }
        } catch (Exception ignored) {}

        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                channel.shutdownNow();
            }
        }
        System.out.println("[GrpcClient] Desconectado com sucesso.");
    }

    @Override
    public boolean isConnected() {
        return connected && channel != null && !channel.isShutdown();
    }

    @Override
    public String getUsername() {
        return username;
    }
}
