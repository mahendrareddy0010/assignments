import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import raft.Network;
import raft.rpcMessages.NetworkCallResponse;
import raft.rpcMessages.RaftState;

public class Client {
    public static void main(String[] args) {
        Random random = new Random();
        List<Integer> clusterIds = new ArrayList<>();
        int myId = Integer.valueOf(args[args.length - 1]);
        for (int i = 0; i < args.length - 1; i = i + 1) {
            int arg = Integer.valueOf(args[i]);
            System.out.println(arg);
            clusterIds.add(arg);
        }
        Network net = new Network(clusterIds, myId);

        // let the connections build
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        int leaderId = clusterIds.get(random.nextInt(3));
        for (int i = 0; i < 10; i = i + 1) {
            NetworkCallResponse response = net.call(leaderId, "broadcastMsg", "msg" + i);
            if (response.isSuccess()) {
                if ((Boolean) response.getBody() == true) {
                    // print("msg" + i);
                } else {
                    leaderId = clusterIds.get(random.nextInt(3)); // [0, 3)
                    print("trying new leader id: " + leaderId);
                }
            }
        }
        List<RaftState> states = new ArrayList<>();
        int minCommitLength = Integer.MAX_VALUE;
        for (int serverId : clusterIds) {
            NetworkCallResponse response = net.call(serverId, "takeSnapShot", "");
            if (response.isSuccess()) {
                RaftState state = (RaftState) response.getBody();
                states.add(state);
                minCommitLength = Math.min(minCommitLength, state.getCommitLength());
            } else {
                print(String.format("****************[%s]not able to take SnapShot***************", serverId));
            }

        }
        print("Committed msgs Length : "+String.valueOf(minCommitLength));
        for (int i = 0; i < minCommitLength; i = i + 1) {
            String msg0 = states.get(0).getLog().get(i).toString();
            String msg1 = states.get(1).getLog().get(i).toString();
            String msg2 = states.get(2).getLog().get(i).toString();
            if (!msg0.equals(msg1)) {
                print("it is wrong at index : 0-1 : " + i + " :" + msg0 + ":" + msg1);
            }
            if (!msg1.equals(msg2)) {
                print("it is wront at index : 1-2 : " + i + " :" + msg1 + ":" + msg2);
            }
        }
    }

    private static void print(String msg) {
        System.out.println(msg);
    }
}
