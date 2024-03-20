package raft;

import java.util.Random;

public class Utils {
    public static long getElectionTimeout() {
        final int ELECTION_TIMEOUT_START = 5000;
        final int ELECTION_TIMEOUT_END = 10000;

        Random random = new Random();

        return random.nextLong(ELECTION_TIMEOUT_START, ELECTION_TIMEOUT_END);
    }

    public static long getHeartBeatTimeout() {
        final int BROADCAST_TIMEOUT = 1000;

        return BROADCAST_TIMEOUT;
    }
}
