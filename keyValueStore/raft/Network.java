package raft;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Network {
    Random random = new Random();
    int myId;
    List<Integer> clusterIds;
    Map<Integer, RaftNode> raftNodeObjects;
    Map<Integer, Boolean> repairStatus;

    public Network(List<Integer> clusterIds, int myId) {
        this.myId = myId;
        this.clusterIds = clusterIds;
        Thread.ofVirtual().unstarted(() -> {
            this.establishAllConnections();
        }).start();
        this.raftNodeObjects = new HashMap<>();
        this.repairStatus = new ConcurrentHashMap<>();
        for (int peerId : clusterIds) {
            if (peerId != myId) {
                repairStatus.put(peerId, true);
            }
        }
    }

    private void print(String message) {
        System.out.println(message);
    }

    // blocking call
    private void establishConnection(int targetId) {
        raftNodeObjects.put(targetId, null);
        while (true) {
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry("localhost", targetId);
                try {
                    RaftNode stub = (RaftNode) registry.lookup("RaftNode");
                    raftNodeObjects.put(targetId, stub);
                    print("Connected : " + targetId);
                    break;
                } catch (Exception e) {
                    print("Exception  :" + e.getMessage());
                }
            } catch (RemoteException e) {
                print("Exception : " + e.getMessage());
            }
            try {
                // print("I will again try in 5-10 secs for " + targetId + " : " + Thread.currentThread().threadId()
                //         + " : "
                //         + System.currentTimeMillis() / 1000);
                Thread.sleep(random.nextInt(5000, 10000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        repairStatus.put(targetId, false);
        // print("Exiting the connection establising Thread");
    }

    // non-blocking call
    private void establishAllConnections() {
        for (int peerId : clusterIds) {
            if (peerId != myId) {
                repairStatus.put(peerId, true);
                Thread.ofVirtual().unstarted(() -> {
                    establishConnection(peerId);
                }).start();
            }
        }
    }

    // non-blocking call
    private synchronized void repairConnection(int targetId) {
        if (repairStatus.get(targetId) == false) {
            repairStatus.put(targetId, true);
            Thread.ofVirtual().unstarted(() -> {
                establishConnection(targetId);
            }).start();
        }
    }

    // blocking call
    public NetworkCallResponse call(int targetId, String methodName, String msg) {
        NetworkCallResponse response = new NetworkCallResponse("", false);
        int attemptCount = 0;
        while (attemptCount < 5) {
            if (raftNodeObjects.get(targetId) != null) {
                try {
                    String res = raftNodeObjects.get(targetId).receiveMsg(msg);

                    response = new NetworkCallResponse(res, true);
                    break;
                } catch (Exception e) {
                    repairConnection(targetId);
                }
            } else {
                repairConnection(targetId);
            }
            try {
                // cooling period
                Thread.sleep(random.nextInt(2000, 3000));
            } catch (InterruptedException e) {
            }
            attemptCount += 1;
        }
        if (response.success == false) {
            print("call was unsuccessfull : " + myId + " --> " + targetId);
        }
        return response;
    }

}
