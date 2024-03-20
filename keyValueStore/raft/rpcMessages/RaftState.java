package raft.rpcMessages;

import java.io.Serializable;
import java.util.List;

import raft.LogEntry;

public class RaftState implements Serializable {
    int currentTerm;
    Integer votedFor;
    List<LogEntry> log;
    int commitLength;

    public RaftState(int currentTerm, Integer votedFor, List<LogEntry> log, int commitLength) {
        this.currentTerm = currentTerm;
        this.votedFor = votedFor;
        this.log = log;
        this.commitLength = commitLength;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(int term) {
        currentTerm = term;
    }

    public Integer getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(int id) {
        votedFor = id;
    }

    public List<LogEntry> getLog() {
        return log;
    }

    public void setLog(List<LogEntry> log) {
        this.log = log;
    }

    public int getCommitLength() {
        return commitLength;
    }

    public void setCommitLength(int length) {
        commitLength = length;
    }

}
