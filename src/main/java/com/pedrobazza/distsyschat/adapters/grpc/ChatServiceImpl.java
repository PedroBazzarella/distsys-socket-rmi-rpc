package com.pedrobazza.distsyschat.adapters.grpc;

import com.pedrobazza.distsyschat.core.domain.Message;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {
    private final Map<String, StreamObserver<ChatMessage>> clients = new ConcurrentHashMap<>();

    @Override
    public void streamMessages(ConnectRequest request, StreamObserver<ChatMessage> responseObserver) {
        String username = request.getUsername();
        clients.put(username, responseObserver);
        System.out.println("[GrpcServer] Cliente conectado ao stream: " + username + " (Total: " + clients.size() + ")");

        broadcast(ChatMessage.newBuilder()
                .setSender("SERVER")
                .setContent(username + " entrou no chat.")
                .setTimestamp(System.currentTimeMillis())
                .setRecipient("ALL")
                .build());
    }

    @Override
    public void sendMessage(ChatMessage request, StreamObserver<SendResponse> responseObserver) {
        broadcast(request);
        responseObserver.onNext(SendResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Mensagem processada com sucesso.")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void disconnect(DisconnectRequest request, StreamObserver<DisconnectResponse> responseObserver) {
        String username = request.getUsername();
        StreamObserver<ChatMessage> observer = clients.remove(username);
        if (observer != null) {
            try {
                observer.onCompleted();
            } catch (Exception ignored) {}
            System.out.println("[GrpcServer] Cliente desconectado: " + username + " (Restantes: " + clients.size() + ")");
            broadcast(ChatMessage.newBuilder()
                    .setSender("SERVER")
                    .setContent(username + " saiu do chat.")
                    .setTimestamp(System.currentTimeMillis())
                    .setRecipient("ALL")
                    .build());
        }
        responseObserver.onNext(DisconnectResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    public void broadcast(ChatMessage message) {
        System.out.println(String.format("[GrpcServer] Broadcast: [%s] %s: %s",
                message.getSender(), message.getSender(), message.getContent()));

        clients.forEach((user, observer) -> {
            try {
                observer.onNext(message);
            } catch (Exception e) {
                System.err.println("[GrpcServer] Falha ao enviar para " + user + ", removendo.");
                clients.remove(user);
            }
        });
    }

    public void broadcast(Message message) {
        ChatMessage grpcMsg = ChatMessage.newBuilder()
                .setSender(message.getSender())
                .setContent(message.getContent())
                .setTimestamp(message.getTimestamp())
                .setRecipient(message.getRecipient())
                .build();
        broadcast(grpcMsg);
    }

    public int getConnectedClientsCount() {
        return clients.size();
    }

    public void clearClients() {
        clients.forEach((user, observer) -> {
            try {
                observer.onCompleted();
            } catch (Exception ignored) {}
        });
        clients.clear();
    }
}
