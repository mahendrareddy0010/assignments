package raft;

import java.io.Serializable;

public class LogEntry implements Serializable{
    String msg;
    int term;

    public LogEntry(String msg, int term) {
        this.msg = msg;
        this.term = term;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    @Override
    public String toString() {
        return msg + ":"+ term;
    }
}
