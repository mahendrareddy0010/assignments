package raft;

import java.rmi.Remote;
import java.rmi.RemoteException;

import raft.rpcMessages.LogRequestArgs;
import raft.rpcMessages.LogResponseArgs;
import raft.rpcMessages.VoteRequestArgs;
import raft.rpcMessages.VoteResponseArgs;

public interface RaftNode extends Remote {
    public String receiveMsg(String msg) throws RemoteException;
    public VoteResponseArgs voteRequest(VoteRequestArgs voteRequestArgs) throws RemoteException;
    public LogResponseArgs logRequest(LogRequestArgs logRequestArgs) throws RemoteException;
}
