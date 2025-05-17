package client;

import shared.Nodo;

import java.rmi.Naming;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Controller {

    private static final List<String> NODOS_VALIDOS = Arrays.asList("Nodo1", "Nodo2", "Nodo3");

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Sistema Distribuído - Envio de Mensagens ===");

        while (true) {
            System.out.println("\nEscolha um nodo para enviar a mensagem (" + String.join(", ", NODOS_VALIDOS) + ")");
            System.out.print("Digite o nome do nodo (ou 'sair'): ");
            String nome = sc.nextLine().trim();

            if (nome.equalsIgnoreCase("sair")) {
                System.out.println("Encerrando controlador...");
                break;
            }

            if (!NODOS_VALIDOS.contains(nome)) {
                System.out.println("⚠️ Nodo inválido. Use um dos seguintes: " + NODOS_VALIDOS);
                continue;
            }

            try {
                Nodo nodo = (Nodo) Naming.lookup("rmi://localhost/" + nome);

                System.out.print("Digite sua mensagem: ");
                String msg = sc.nextLine().trim();

                int timestamp = (int) (System.currentTimeMillis() / 1000);
                nodo.broadcast(msg, timestamp, nome);
                System.out.println("✅ Mensagem enviada para " + nome + " com timestamp " + timestamp);

            } catch (Exception e) {
                System.out.println("❌ Erro ao conectar com o nodo " + nome);
                e.printStackTrace();
            }
        }

        sc.close();
    }
}