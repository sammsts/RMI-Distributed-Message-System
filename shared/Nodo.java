// 1. Interface remota compartilhada entre os nós
package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Nodo extends Remote {
    void broadcast(String mensagem, int timestamp, String remetente) throws RemoteException;
    void ack(String mensagemId, String remetente) throws RemoteException;
    void nack(String mensagemId, String remetente) throws RemoteException;
    void deliver(String mensagem, String remetente) throws RemoteException;
}