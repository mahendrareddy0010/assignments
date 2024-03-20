package raft.rpcMessages;

import java.io.Serializable;

public class LogResponseArgs implements Serializable {
    int followerId;
    int term;
    int ack;
    boolean success;

    public LogResponseArgs(int followerId, int term, int ack, boolean success) {
        this.followerId = followerId;
        this.term = term;
        this.ack = ack;
        this.success = success;
    }

    public int getFollowerId() {
        return followerId;
    }

    public void setFollowerId(int id) {
        this.followerId = id;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    public boolean isSuccess() {
        return success;
    }
}
