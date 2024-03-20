package raft.rpcMessages;

import java.io.Serializable;
import java.util.List;

import raft.LogEntry;

public class LogRequestArgs implements Serializable {
    int leaderId;
    int term;
    int logLength;
    int logTerm;
    int leaderCommit;
    List<LogEntry> entries;

    public LogRequestArgs(int leaderId, int term, int logLength, int logTerm, int leaderCommit,
            List<LogEntry> entries) {
                this.leaderId = leaderId;
                this.term = term;
                this.logLength = logLength;
                this.logTerm = logTerm;
                this.leaderCommit = leaderCommit;
                this.entries = entries;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int id) {
        leaderId = id;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    
    public int getLogLength() {
        return logLength;
    }

    public void setLogLength(int logLength) {
        this.logLength = logLength;
    }

    
    public int getLogTerm() {
        return logTerm;
    }

    public void setLogTerm(int logTerm) {
        this.logTerm = logTerm;
    }

    
    public int getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(int leaderCommit) {
        this.leaderCommit = leaderCommit;
    }

    public List<LogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries;
    }

}
