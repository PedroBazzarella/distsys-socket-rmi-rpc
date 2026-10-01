package com.pedrobazza.distsyschat.cli;

import com.pedrobazza.distsyschat.core.domain.Message;
import com.pedrobazza.distsyschat.core.ports.ChatClientPort;
import com.pedrobazza.distsyschat.core.ports.MessageListener;

import java.util.Scanner;

public class Cli implements MessageListener {
    private final String username;

    public Cli(String username) {
        this.username = username;
    }

    @Override
    public void onMessageReceived(Message message) {
        if ("SERVER".equalsIgnoreCase(message.getSender())) {
            System.out.println("\n*** [INFO] " + message.getContent() + " ***");
        } else if (username.equalsIgnoreCase(message.getSender())) {
            // Mensagem enviada pelo próprio usuário recebida via echo/broadcast
            System.out.println(String.format("[%s] Você: %s", message.getFormattedTime(), message.getContent()));
        } else {
            System.out.println(String.format("[%s] %s: %s", message.getFormattedTime(), message.getSender(), message.getContent()));
        }
        System.out.print("> ");
        System.out.flush();
    }

    public void start(ChatClientPort client) {
        System.out.println("==================================================");
        System.out.println(" Bem-vindo ao Chat Distribuído (" + client.getClass().getSimpleName() + ")");
        System.out.println(" Usuário: " + username);
        System.out.println(" Comandos disponíveis: /sair, /help");
        System.out.println("==================================================");

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("> ");
            System.out.flush();

            while (client.isConnected() && scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    System.out.print("> ");
                    System.out.flush();
                    continue;
                }

                if (line.equalsIgnoreCase("/sair") || line.equalsIgnoreCase("/exit") || line.equalsIgnoreCase("/quit")) {
                    System.out.println("Encerrando sessão...");
                    break;
                } else if (line.equalsIgnoreCase("/help")) {
                    System.out.println("--- Ajuda ---");
                    System.out.println(" Digite sua mensagem e pressione ENTER para enviar.");
                    System.out.println(" Digite /sair para desconectar do chat.");
                    System.out.println("-------------");
                } else {
                    try {
                        client.sendMessage(line);
                    } catch (Exception e) {
                        System.err.println("Erro ao enviar mensagem: " + e.getMessage());
                        break;
                    }
                }

                System.out.print("> ");
                System.out.flush();
            }
        } finally {
            try {
                client.disconnect();
            } catch (Exception ignored) {}
        }
    }
}
