package com.pedrobazza.distsyschat.app;

import com.pedrobazza.distsyschat.cli.Cli;
import com.pedrobazza.distsyschat.core.ports.ChatClientPort;
import com.pedrobazza.distsyschat.network.NetworkFactory;

import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        String protocol = null;
        String host = null;
        int port = 0;
        String username = null;

        if (args.length >= 4) {
            protocol = args[0].trim();
            host = args[1].trim();
            try {
                port = Integer.parseInt(args[2].trim());
            } catch (NumberFormatException e) {
                System.err.println("Porta inválida: " + args[2]);
                printUsage();
                return;
            }
            username = args[3].trim();
        } else {
            // Se não forem informados todos argumentos pela linha de comando, solicita interativamente
            Scanner scanner = new Scanner(System.in);
            System.out.println("=== Configuração do Cliente de Chat ===");
            System.out.print("Selecione o protocolo (socket / grpc / rmi): ");
            protocol = scanner.nextLine().trim();

            String defaultHost = switch (protocol.toLowerCase()) {
                case "socket", "sockets" -> "server-socket";
                case "grpc", "rpc" -> "server-grpc";
                case "rmi" -> "server-rmi";
                default -> "localhost";
            };

            int defaultPort = switch (protocol.toLowerCase()) {
                case "socket", "sockets" -> 8080;
                case "grpc", "rpc" -> 50051;
                case "rmi" -> 1099;
                default -> 8080;
            };

            System.out.print("Informe o host do servidor [" + defaultHost + "]: ");
            String inputHost = scanner.nextLine().trim();
            host = inputHost.isEmpty() ? defaultHost : inputHost;

            System.out.print("Informe a porta do servidor [" + defaultPort + "]: ");
            String inputPort = scanner.nextLine().trim();
            try {
                port = inputPort.isEmpty() ? defaultPort : Integer.parseInt(inputPort);
            } catch (NumberFormatException e) {
                System.out.println("Porta inválida. Usando padrão: " + defaultPort);
                port = defaultPort;
            }

            System.out.print("Informe o seu nome de usuário: ");
            username = scanner.nextLine().trim();
            while (username.isEmpty()) {
                System.out.print("Nome de usuário não pode ser vazio. Digite novamente: ");
                username = scanner.nextLine().trim();
            }
        }

        try {
            ChatClientPort client = NetworkFactory.createClient(protocol);
            Cli cli = new Cli(username);

            System.out.println(String.format("Conectando a %s:%d via %s como '%s'...", host, port, protocol.toUpperCase(), username));
            client.connect(host, port, username, cli);

            // Inicia interface interativa no terminal
            cli.start(client);
            System.exit(0);
        } catch (IllegalArgumentException e) {
            System.err.println("Erro nos parâmetros: " + e.getMessage());
            printUsage();
        } catch (Exception e) {
            System.err.println("Falha ao conectar ao servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("\nUso correto:");
        System.out.println("  java -jar client.jar <protocolo> <host> <porta> <username>");
        System.out.println("Exemplos:");
        System.out.println("  java -jar client.jar socket localhost 8080 Alice");
        System.out.println("  java -jar client.jar grpc localhost 50051 Bob");
        System.out.println("  java -jar client.jar rmi localhost 1099 Carol");
    }
}

