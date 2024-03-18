package main;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import mr.CoordinateRPCInterface;
import mr.Worker;
import mrApplication.WordCount;

public class MapReduceWorker {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099); // Connect to the RMI registry
            CoordinateRPCInterface remoteObj = (CoordinateRPCInterface) registry.lookup("Coordinator");
            new Worker(new WordCount(), remoteObj);
        } catch (Exception e) {
            System.out.println("Wroker unable to contact remote registry" + e);
            System.out.println("......Exiting the worker..........");
        }        
    }
}
