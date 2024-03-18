package mr;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CoordinatorServer {
    public static void start(String directoryName) {
        try {
            // CoordinatorRPCImpl coordinatorSkelton = (CoordinatorRPCImpl) UnicastRemoteObject
            //         .exportObject(new CoordinatorRPCImpl(directoryName, 10), 1099);
            // // Bind the remote object's stub in the registry
            // Registry registry = LocateRegistry.getRegistry();

            CoordinatorRPCImpl coordinatorSkelton = new CoordinatorRPCImpl(directoryName, 10);
            Registry registry = LocateRegistry.createRegistry(1099); // Default RMI registry port
            registry.rebind("Coordinator", coordinatorSkelton);

            System.err.println("Coordinator is ready : " + registry);

            while (!coordinatorSkelton.isDone()) {
                Thread.sleep(5000);
            }
            System.out.println("................Server exiting..................");
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Coordinator unable to start : " + e);
        }
    }
}
