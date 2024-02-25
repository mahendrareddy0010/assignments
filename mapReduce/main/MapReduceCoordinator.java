package mapReduce.main;

import mapReduce.mr.CoordinatorServer;

public class MapReduceCoordinator {
    public static void main(String[] args) {
        CoordinatorServer.start("./");
    }
}
