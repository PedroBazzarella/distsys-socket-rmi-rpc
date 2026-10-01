package com.pedrobazza.distsyschat.network;

import com.pedrobazza.distsyschat.adapters.grpc.GrpcClientAdapter;
import com.pedrobazza.distsyschat.adapters.grpc.GrpcServerAdapter;
import com.pedrobazza.distsyschat.adapters.rmi.RmiClientAdapter;
import com.pedrobazza.distsyschat.adapters.rmi.RmiServerAdapter;
import com.pedrobazza.distsyschat.adapters.sockets.SocketClientAdapter;
import com.pedrobazza.distsyschat.adapters.sockets.SocketServerAdapter;
import com.pedrobazza.distsyschat.core.ports.ChatClientPort;
import com.pedrobazza.distsyschat.core.ports.ChatServerPort;

public class NetworkFactory {

    public static ChatServerPort createServer(String protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException("Protocolo não pode ser nulo.");
        }
        return switch (protocol.trim().toLowerCase()) {
            case "socket", "sockets" -> new SocketServerAdapter();
            case "grpc", "rpc" -> new GrpcServerAdapter();
            case "rmi" -> new RmiServerAdapter();
            default -> throw new IllegalArgumentException("Protocolo de servidor desconhecido: '" + protocol
                    + "'. Opções válidas: socket, grpc, rmi");
        };
    }

    public static ChatClientPort createClient(String protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException("Protocolo não pode ser nulo.");
        }
        return switch (protocol.trim().toLowerCase()) {
            case "socket", "sockets" -> new SocketClientAdapter();
            case "grpc", "rpc" -> new GrpcClientAdapter();
            case "rmi" -> new RmiClientAdapter();
            default -> throw new IllegalArgumentException("Protocolo de cliente desconhecido: '" + protocol
                    + "'. Opções válidas: socket, grpc, rmi");
        };
    }
}
