package raft;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Random;

public class RaftNodeImpl extends UnicastRemoteObject implements RaftNode {
    Random random = new Random();
    int myId;
    List<Integer> clusterIds;
    Network net;

    public RaftNodeImpl(List<Integer> clusterIds, int myId) throws RemoteException {
        super();
        net = new Network(clusterIds, myId);
        this.myId = myId;
        this.clusterIds = clusterIds;
    }

    public void broadcastMsg(String msg) {
        for (int peerId : clusterIds) {
            if (peerId != myId) {
                Thread.ofVirtual().unstarted(() -> {
                    NetworkCallResponse response = net.call(peerId, "receiveMsg", msg);
                    if (response.success) {
                        System.out.println(response.body);
                    } else {

                    }
                }).start();
            }
        }
    }

    @Override
    public String receiveMsg(String msg) {
        print("Received msg : " + msg);
        print("processing the message for 50-100 secs");
        print("Thread name : " + Thread.currentThread());
        for (int i = 0; i < 10; i += 1) {
            System.out.println("I am in Thread : " + Thread.currentThread().threadId() + " : " + i);
            try {
                Thread.sleep(random.nextInt(10000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return "Hello from :" + myId;
    }

    private void print(String message) {
        System.out.println(message);
    }
}
