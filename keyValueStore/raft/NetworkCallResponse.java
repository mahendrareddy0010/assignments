package raft;

public class NetworkCallResponse {
    Object body;
    boolean success;

    public NetworkCallResponse(Object body, boolean success) {
        this.body = body;
        this.success = success;
    }
}
