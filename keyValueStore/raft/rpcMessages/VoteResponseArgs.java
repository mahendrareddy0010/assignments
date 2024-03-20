package raft.rpcMessages;

import java.io.Serializable;

public class VoteResponseArgs implements Serializable {
    int voterId;
    int voterTerm;
    boolean granted;

    public VoteResponseArgs(int voterId, int voterTerm, boolean granted) {
        this.voterId = voterId;
        this.voterTerm = voterTerm;
        this.granted = granted;
    }

    public int getVoterId() {
        return voterId;
    }

    public void setVoterId(int id) {
        voterId = id;
    }

    public int getVoterTerm() {
        return voterTerm;
    }

    public void setVoterTerm(int term) {
        voterTerm = term;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean grant) {
        granted = grant;
    }

    @Override
    public String toString() {
        return "voterId : " + voterId + "\n" + "voterTerm : " + voterTerm + "\n" + "granted : " + granted;
    }
}
