package raft;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import raft.rpcMessages.LogRequestArgs;
import raft.rpcMessages.LogResponseArgs;
import raft.rpcMessages.NetworkCallResponse;
import raft.rpcMessages.RaftState;
import raft.rpcMessages.VoteRequestArgs;
import raft.rpcMessages.VoteResponseArgs;

public class RaftNodeImpl extends UnicastRemoteObject implements RaftNode {
    RaftTimer electionTimer;
    RaftTimer heartBeatTimer;

    Random random = new Random();
    int myId;
    List<Integer> clusterIds;
    Network net;

    // have to be persisted
    int currentTerm;
    Integer votedFor;
    List<LogEntry> log;
    int commitLength;

    // no need to persist
    Role currentRole;
    Integer currentLeader;
    Set<Integer> votesReceived;
    Map<Integer, Integer> sentLength;
    Map<Integer, Integer> ackedLength;

    public RaftNodeImpl(List<Integer> clusterIds, int myId) throws RemoteException {
        super();
        net = new Network(clusterIds, myId);
        this.myId = myId;
        this.clusterIds = clusterIds;

        initialize();
    }

    // it is blocking call
    private void initialize() {
        // this has to come from file if persisted
        currentTerm = 0;
        votedFor = null;
        log = new ArrayList<>();
        commitLength = 0;

        // volatile state
        currentRole = Role.FOLLOWER;
        currentLeader = null;
        votesReceived = new HashSet<>();
        sentLength = new HashMap<>();
        ackedLength = new HashMap<>();

        //
        for (int peerId : clusterIds) {
            sentLength.put(peerId, log.size());
            ackedLength.put(peerId, 0);
        }

        // timer
        startElectionTimer();
    }

    private void startElectionTimer() {
        // timers
        electionTimer = new RaftTimer();
        electionTimer.scheduleTask(Utils.getElectionTimeout(), () -> {
            startElection();
        });
    }

    private synchronized void startElection() {
        // print("*************************Started Election at : " + myId);

        currentTerm = currentTerm + 1;
        currentRole = Role.CANDIDATE;
        votedFor = myId;
        votesReceived = new HashSet<>();
        votesReceived.add(myId);

        int lastTerm = 0;
        if (log.size() > 0) {
            lastTerm = log.get(log.size() - 1).term;
        }
        VoteRequestArgs voteRequestArgs = new VoteRequestArgs(myId, currentTerm, log.size(), lastTerm);
        for (int peerId : clusterIds) {
            if (peerId != myId) {
                Thread.ofVirtual().unstarted(() -> {
                    NetworkCallResponse response = net.call(peerId, "voteRequest", voteRequestArgs);
                    if (response.isSuccess()) {
                        voteResponse((VoteResponseArgs) response.getBody());
                    } else {

                    }
                }).start();
            }
        }

        // print("----------------------Scheduling Election at : " + myId);
        electionTimer.resetTimeout(Utils.getElectionTimeout());
    }

    private void startHeartBeat() {
        heartBeatTimer = new RaftTimer();
        heartBeatTimer.scheduleTask(Utils.getHeartBeatTimeout(), () -> {
            heartBeat();
        });
    }

    private synchronized void heartBeat() {
        if (currentRole == Role.LEADER) {
            for (int followerId : clusterIds) {
                if (followerId != myId) {
                    replicateLogTo(myId, followerId);
                }
            }
            heartBeatTimer.resetTimeout(Utils.getHeartBeatTimeout());
        }
    }

    public synchronized VoteResponseArgs voteRequest(VoteRequestArgs voteRequestArgs) {
        int cId = voteRequestArgs.getCId();
        int cTerm = voteRequestArgs.getCTerm();
        int cLogLength = voteRequestArgs.getCLogLength();
        int cLogTerm = voteRequestArgs.getCLogTerm();

        int myLogTerm = 0;
        if (log.size() > 0) {
            myLogTerm = log.get(log.size() - 1).term;
        }

        boolean logOk = (cLogTerm > myLogTerm) || (cLogTerm == myLogTerm && cLogLength >= log.size());
        boolean termOk = (cTerm > currentTerm) || (cTerm == currentTerm && (votedFor == null || votedFor == cId));

        VoteResponseArgs voteResponseArgs;

        if (termOk && logOk) {
            currentTerm = cTerm;
            currentRole = Role.FOLLOWER;
            votedFor = cId;
            voteResponseArgs = new VoteResponseArgs(myId, currentTerm, true);
        } else {
            voteResponseArgs = new VoteResponseArgs(myId, currentTerm, false);
        }

        return voteResponseArgs;
    }

    private synchronized void voteResponse(VoteResponseArgs voteResponseArgs) {
        int voterId = voteResponseArgs.getVoterId();
        int voterTerm = voteResponseArgs.getVoterTerm();
        boolean granted = voteResponseArgs.isGranted();

        // print("Vote Response from : " + voterId);
        // print(voteResponseArgs.toString());

        if (currentRole == Role.CANDIDATE && currentTerm == voterTerm && granted) {
            votesReceived.add(voterId);
            if (votesReceived.size() >= (clusterIds.size() + 1) / 2) {
                currentRole = Role.LEADER;
                currentLeader = myId;
                print("I am the Leader now : " + myId);
                print("State: " + "curremtTerm : " + currentTerm + " votesRecieved : " + votesReceived);
                electionTimer.cancelTimeout();
                startHeartBeat();
                for (int followerId : clusterIds) {
                    if (followerId != myId) {
                        sentLength.put(followerId, log.size());
                        ackedLength.put(followerId, 0);
                        replicateLogTo(myId, followerId);
                    }
                }
            }
        } else if (voterTerm > currentTerm) {
            currentTerm = voterTerm;
            currentRole = Role.FOLLOWER;
            votedFor = null;
            // I will start new election
            electionTimer.resetTimeout(Utils.getElectionTimeout());
        }
        // print("my state is : " + "currentTerm : " + currentTerm + "currentRole : " +
        // currentRole + "currentLeader : "
        // + currentLeader + "votesReceived : " + votesReceived);
    }

    private synchronized void replicateLogTo(int leaderId, int followerId) {
        // print("replicating log to : " + followerId);
        int startIdx = sentLength.get(followerId);
        List<LogEntry> entries = new ArrayList<>();
        for (int j = startIdx; j < log.size(); j = j + 1) {
            entries.add(log.get(j));
        }
        int prevLogTerm = 0;
        if (startIdx > 0) {
            prevLogTerm = log.get(log.size() - 1).term;
        }

        LogRequestArgs logRequestArgs = new LogRequestArgs(leaderId, currentTerm, startIdx, prevLogTerm, commitLength,
                entries);

        Thread.ofVirtual().unstarted(() -> {
            // print("Making call to follower : " + followerId);
            NetworkCallResponse response = net.call(followerId, "logRequest", logRequestArgs);
            if (response.isSuccess()) {
                logResponse((LogResponseArgs) response.getBody());
            }
        }).start();
    }

    public synchronized RaftState takeSnapShot() {
        // don't need to clone because it is synchronized, no one else would modify it now
        RaftState raftState = new RaftState(currentTerm, votedFor, log, commitLength);

        return raftState;
    }

    public synchronized Boolean broadcastMsg(String msg) {
        if (currentRole == Role.LEADER) {
            LogEntry logEntry = new LogEntry(msg, currentTerm);
            log.add(logEntry);
            ackedLength.put(myId, log.size());
            for (int followerId : clusterIds) {
                if (followerId != myId) {
                    replicateLogTo(myId, followerId);
                }
            }
            return true;
        }

        return false;
    }

    public synchronized LogResponseArgs logRequest(LogRequestArgs logRequestArgs) {
        // reset election timer
        if (currentRole == Role.LEADER) {
            startElection();
        } else {
            electionTimer.resetTimeout(Utils.getElectionTimeout());
        }

        int leaderId = logRequestArgs.getLeaderId();
        int term = logRequestArgs.getTerm();
        int logLength = logRequestArgs.getLogLength();
        int logTerm = logRequestArgs.getLogTerm();
        int leaderCommit = logRequestArgs.getLeaderCommit();
        List<LogEntry> entries = logRequestArgs.getEntries();

        if (term > currentTerm) {
            currentTerm = term;
            votedFor = null;
            currentRole = Role.FOLLOWER;
            currentLeader = leaderId;
        }
        if (term == currentTerm && currentRole == Role.CANDIDATE) {
            currentRole = Role.FOLLOWER;
            currentLeader = leaderId;
        }

        boolean logOk = (log.size() >= logLength) && (logLength == 0 || logTerm == log.get(log.size() - 1).term);

        LogResponseArgs logResponseArgs;
        if (term == currentTerm && logOk) {
            appendEntries(logLength, leaderCommit, entries);
            int ack = logLength + entries.size();
            logResponseArgs = new LogResponseArgs(myId, currentTerm, ack, true);
        } else {
            logResponseArgs = new LogResponseArgs(myId, currentTerm, 0, false);
        }

        return logResponseArgs;
    }

    private synchronized void appendEntries(int logLength, int leaderCommit, List<LogEntry> entries) {
        if (entries.size() > 0 && log.size() > logLength) {
            if (log.get(logLength).term != entries.get(0).term) {
                for (int i = 0; i < log.size() - logLength; i = i + 1) {
                    log.removeLast();
                }
            }
        }
        if (logLength + entries.size() > log.size()) {
            for (int i = log.size() - logLength; i < entries.size(); i = i + 1) {
                log.add(entries.get(i));
            }
        }
        if (leaderCommit > commitLength) {
            commitLength = leaderCommit;
        }
    }

    public synchronized void logResponse(LogResponseArgs logResponseArgs) {
        if (logResponseArgs != null) {
            int followerId = logResponseArgs.getFollowerId();
            int term = logResponseArgs.getTerm();
            int ack = logResponseArgs.getAck();
            boolean success = logResponseArgs.isSuccess();
            if (term == currentTerm && currentRole == Role.LEADER) {
                if (success == true && ack >= ackedLength.get(followerId)) {
                    sentLength.put(followerId, ack);
                    ackedLength.put(followerId, ack);
                    commitLogEntries();
                } else if (sentLength.get(followerId) > 0) {
                    sentLength.put(followerId, sentLength.get(followerId) - 1);
                    replicateLogTo(myId, followerId);
                }
            } else if (term > currentTerm) {
                currentTerm = term;
                currentRole = Role.FOLLOWER;
                votedFor = null;
            }
        }
    }

    private int acks(int length) {
        int cnt = 0;
        for (int peerId : clusterIds) {
            if (ackedLength.get(peerId) >= length) {
                cnt = cnt + 1;
            }
        }

        return cnt;
    }

    private void commitLogEntries() {
        int minAcks = (clusterIds.size() + 1) / 2;
        int maxReady = commitLength;
        for (int len = commitLength; len < log.size(); len = len + 1) {
            if (acks(len) >= minAcks) {
                maxReady = len;
            }
        }
        if (maxReady > commitLength && log.get(maxReady - 1).term == currentTerm) {
            for (int i = commitLength; i < maxReady; i = i + 1) {
                print("Delivered : " + log.get(i));
            }
            commitLength = maxReady;
        }
    }

    // does not require synchronized because we are not accessing any modifiable
    // state
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
