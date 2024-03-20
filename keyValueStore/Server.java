import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import raft.RaftNodeImpl;

public class Server {
    public static void main(String[] args) {
        List<Integer> clusterIds = new ArrayList<>();
        int myId = Integer.valueOf(args[args.length - 1]);
        for (int i = 0; i < args.length - 1; i = i + 1) {
            int arg = Integer.valueOf(args[i]);
            System.out.println(arg);
            clusterIds.add(arg);
        }
        try {
            RaftNodeImpl raftNodeSkelton = new RaftNodeImpl(clusterIds, myId);
            Registry registry = LocateRegistry.createRegistry(myId);
            registry.rebind("RaftNode", raftNodeSkelton);

            System.out.println("Raft Node is ready : " + myId);
            // if (myId == 1201) {
            //     raftNodeSkelton.broadcastMsg("My name is - 1 :" + myId);
            //     raftNodeSkelton.broadcastMsg("My name is - 2 :" + myId);
            // }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
