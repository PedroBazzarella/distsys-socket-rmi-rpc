package com.pedrobazza.distsyschat.app;

import com.pedrobazza.distsyschat.core.ports.ChatServerPort;
import com.pedrobazza.distsyschat.network.NetworkFactory;

public class ServerApp {
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }

        String protocol = args[0].trim().toLowerCase();
        int defaultPort = switch (protocol) {
            case "socket", "sockets" -> 8080;
            case "grpc", "rpc" -> 50051;
            case "rmi" -> 1099;
            default -> 8080;
        };

        int port = defaultPort;
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1].trim());
            } catch (NumberFormatException e) {
                System.err.println("Porta inválida: " + args[1] + ". Usando porta padrão " + defaultPort);
                port = defaultPort;
            }
        }

        try {
            ChatServerPort server = NetworkFactory.createServer(protocol);
            server.start(port);

            System.out.println("==================================================");
            System.out.println(" Servidor rodando via: " + protocol.toUpperCase());
            System.out.println(" Porta de escuta: " + port);
            System.out.println(" Pressione Ctrl+C para encerrar.");
            System.out.println("==================================================");

            java.util.concurrent.CountDownLatch shutdownLatch = new java.util.concurrent.CountDownLatch(1);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nFinalizando servidor...");
                try {
                    server.stop();
                } catch (Exception e) {
                    System.err.println("Erro ao parar servidor: " + e.getMessage());
                } finally {
                    shutdownLatch.countDown();
                }
            }));

            shutdownLatch.await();
        } catch (IllegalArgumentException e) {
            System.err.println("Erro de configuração: " + e.getMessage());
            printUsage();
        } catch (Exception e) {
            System.err.println("Falha ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("\nUso correto:");
        System.out.println("  java -jar server.jar <protocolo> [porta]");
        System.out.println("Protocolos suportados: socket | grpc | rmi");
        System.out.println("Exemplos:");
        System.out.println("  java -jar server.jar socket 8080");
        System.out.println("  java -jar server.jar grpc 50051");
        System.out.println("  java -jar server.jar rmi 1099");
    }
}