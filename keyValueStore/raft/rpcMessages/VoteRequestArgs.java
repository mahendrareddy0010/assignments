package raft.rpcMessages;

import java.io.Serializable;

public class VoteRequestArgs implements Serializable {
    int cId;
    int cTerm;
    int cLogLength;
    int cLogTerm;

    public VoteRequestArgs(int cId, int cTerm, int cLogLength, int cLogTerm) {
        this.cId = cId;
        this.cTerm = cTerm;
        this.cLogLength = cLogLength;
        this.cLogTerm = cLogTerm;
    }

    public int getCId() {
        return cId;
    }

    public void setCId(int id) {
        cId = id;
    }

    public int getCTerm() {
        return cTerm;
    }

    public void setCTerm(int term) {
        cTerm = term;
    }

    public int getCLogLength() {
        return cLogLength;
    }

    public void setCLogLength(int logLength) {
        cLogLength = logLength;
    }

    public int getCLogTerm() {
        return cLogTerm;
    }

    public void setCLogTerm(int logTerm) {
        cLogTerm = logTerm;
    }

    @Override
    public String toString() {
        return "cId : " + cId + "\n" + "cTerm : " + cTerm + "\n"
                + "cLogLength : " + cLogLength + "\n" + "cLogTerm : " + cLogTerm;
    }
}
