package mapReduce.mr;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CoordinateRPCInterface extends Remote {
    public JobDetailsWrapper getJob() throws RemoteException;
    public void notifyMapJobFinished(String fileName) throws RemoteException;
    public void notifyReduceJobFinished(int reduceWorkerNumber) throws RemoteException;
    public String sayHello() throws RemoteException;
}
