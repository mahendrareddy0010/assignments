package raft;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RaftNode extends Remote {
    public String receiveMsg(String msg) throws RemoteException;
}
