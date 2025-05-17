package server;

import shared.Nodo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.util.*;
import java.util.concurrent.*;

public class NodoImpl extends UnicastRemoteObject implements Nodo {

    private String nome;
    private Set<String> entregues = ConcurrentHashMap.newKeySet();
    private Map<String, String> mensagensRecebidas = new ConcurrentHashMap<>();
    private Map<String, ScheduledFuture<?>> timeouts = new ConcurrentHashMap<>();
    private List<String> vizinhos = new ArrayList<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public NodoImpl(String nome) throws RemoteException {
        this.nome = nome;
    }

    public void setVizinhos(List<String> vizinhos) {
        this.vizinhos = vizinhos;
    }

    @Override
    public synchronized void broadcast(String mensagem, int timestamp, String remetente) throws RemoteException {
        String id = remetente + ":" + timestamp;

        // ✅ Enviar ACK ao remetente sempre
        try {
            Nodo remetenteNodo = (Nodo) Naming.lookup("rmi://localhost/" + remetente);
            remetenteNodo.ack(id, nome);
        } catch (Exception e) {
            System.out.println(nome + " falha ao enviar ACK para " + remetente);
        }

        // ✅ Apenas entrega e processa se ainda não recebeu
        if (!entregues.contains(id)) {
            System.out.println(nome + " recebeu broadcast: " + mensagem + " [" + id + "]");
            mensagensRecebidas.put(id, mensagem);
            entregues.add(id);

            // ✅ Entrega local
            deliver(mensagem, remetente);

            // ✅ Agenda timeout de verificação
            if (nome.equals(remetente)) {
                agendarTimeout(id, remetente, mensagem);
            }

            // ✅ Reenvia para vizinhos (exceto remetente)
            for (String vizinho : vizinhos) {
                if (!vizinho.equals(remetente)) {
                    try {
                        Nodo nodo = (Nodo) Naming.lookup("rmi://localhost/" + vizinho);
                        nodo.broadcast(mensagem, timestamp, remetente);
                    } catch (Exception e) {
                        System.out.println(nome + " falha ao enviar para " + vizinho);
                    }
                }
            }
        }
    }

    @Override
    public void ack(String mensagemId, String remetente) throws RemoteException {
        System.out.println(nome + " recebeu ACK de " + remetente + " para " + mensagemId);
        cancelarTimeout(mensagemId);
    }

    @Override
    public void nack(String mensagemId, String remetente) throws RemoteException {
        System.out.println(nome + " recebeu NACK de " + remetente + " para " + mensagemId);
        retransmitirMensagem(mensagemId);
    }

    @Override
    public synchronized void deliver(String mensagem, String remetente) throws RemoteException {
        String id = remetente + ":" + mensagem.hashCode();
        if (!entregues.contains(id)) {
            entregues.add(id);
            System.out.println("[" + nome + "] entregando mensagem de " + remetente + ": " + mensagem);
        }
    }

    private void agendarTimeout(String id, String remetente, String mensagem) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            System.out.println(nome + " timeout para mensagem " + id);
            retransmitirMensagem(id);
        }, 5, TimeUnit.SECONDS);
        timeouts.put(id, future);
    }

    private void cancelarTimeout(String id) {
        if (timeouts.containsKey(id)) {
            timeouts.get(id).cancel(true);
            timeouts.remove(id);
        }
    }

    private void retransmitirMensagem(String id) {
        if (mensagensRecebidas.containsKey(id)) {
            String[] partes = id.split(":");
            String remetente = partes[0];
            int timestamp = Integer.parseInt(partes[1]);
            String mensagem = mensagensRecebidas.get(id);
            try {
                broadcast(mensagem, timestamp, remetente);
            } catch (Exception e) {
                System.out.println(nome + " erro ao retransmitir " + id);
            }
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Uso: java server.NodoImpl <NomeDoNodo> <Vizinho1,Vizinho2,...>");
                return;
            }
            String nome = args[0];
            NodoImpl nodo = new NodoImpl(nome);
            List<String> vizinhos = Arrays.asList(args[1].split(","));
            nodo.setVizinhos(vizinhos);
            Naming.rebind("rmi://localhost/" + nome, nodo);
            System.out.println("Nodo " + nome + " pronto com vizinhos: " + vizinhos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}