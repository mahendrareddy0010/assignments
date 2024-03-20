package raft.rpcMessages;

public class NetworkCallResponse {
    Object body;
    boolean success;

    public NetworkCallResponse(Object body, boolean success) {
        this.body = body;
        this.success = success;
    }

    public Object getBody() {
        return body;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "body : " + body + "\n"+"success : " + success;
    }
}
