package mapReduce.mr;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoordinatorRPCImpl extends UnicastRemoteObject implements CoordinateRPCInterface {
    Deque<String> allFilesQueue;
    List<String> temporaryFileList;
    String directoryName;
    int totalReduceWorkers;
    boolean isMapPhase = true;
    Set<String> processedFiles;
    Map<String, Long> inProgressFiles;
    boolean isDone = false;

    public CoordinatorRPCImpl(String directoryName, int totalReduceWorkers) throws RemoteException {
        this.directoryName = directoryName;
        this.totalReduceWorkers = totalReduceWorkers;
        this.allFilesQueue = new ArrayDeque<>();
        getAllFilesInDirectory(directoryName);
        this.processedFiles = new HashSet<>();
        this.inProgressFiles = new HashMap<>();
        this.temporaryFileList = new ArrayList<>();
    }

    private void getAllFilesInDirectory(String directoryName) {
        File directory = new File(directoryName);
        String[] allFilesInDirectory = directory.list((dir, name) -> name.endsWith(".txt") && name.startsWith("pg-"));

        for (String file : allFilesInDirectory) {
            allFilesQueue.offerLast(file);
        }
        for (String file : directory.list((dir, name) -> name.endsWith(".txt") && name.startsWith("mr-out-"))) {
            new File(file).delete();
        }
        cleanTemporaryFiles();
    }

    public boolean isDone() {
        return isDone;
    }

    @Override
    public String sayHello() throws RemoteException {
        return null;
    }

    @Override
    public synchronized void notifyReduceJobFinished(int reduceWorkerNumber) {
        // It is ok, even if reduceWorkerNumber already removed by slow worker before
        // new worker finishes
        inProgressFiles.remove(String.valueOf(reduceWorkerNumber)); // it does not raise exception if reduceWorkerNUmber
                                                                    // is not present
        processedFiles.add(String.valueOf(reduceWorkerNumber)); // adding to set is idempotent

        if (allFilesQueue.size() == 0 && inProgressFiles.size() == 0) {
            cleanTemporaryFiles();
            processedFiles.clear();
            inProgressFiles.clear();
            isDone = true;

            System.out.println("...........Reduce Phase finished.......");
            System.out.println(".........Cleared temporary files......");
            System.out.println(".........Done......");
        }
    }

    @Override
    public synchronized void notifyMapJobFinished(String fileName) {
        // It is ok, even if fileName already removed by slow worker before new worker
        // finishes
        inProgressFiles.remove(fileName); // it does not raise exception if reduceWorkerNUmber is not present
        processedFiles.add(fileName); // adding to set is idempotent

        if (allFilesQueue.size() == 0 && inProgressFiles.size() == 0) {
            isMapPhase = false;
            prepareForReducePhase();
            // loads all filenames into var temporaryfileset
            // and also all reducer workers integer into allFilesQueue so that it wil be
            // used
            // for tracking reduce workers
            processedFiles.clear();
            inProgressFiles.clear();
            System.out.println("...........Map Phase finished.......");
        }
    }

    @Override
    public synchronized JobDetailsWrapper getJob() {
        if (isMapPhase) {
            MapJobDetails mapJobDetails = getMapJobDetails();
            if (mapJobDetails != null) {
                return new JobDetailsWrapper(mapJobDetails);
            }
            return null;
        }
        ReduceJobDetails reduceJobDetails = getReduceJobDetails();
        if (reduceJobDetails != null) {
            return new JobDetailsWrapper(reduceJobDetails);
        }

        return null;
    }

    public ReduceJobDetails getReduceJobDetails() {
        if (allFilesQueue.size() != 0) {
            String reduceWorkerNumber = allFilesQueue.pollFirst();
            inProgressFiles.put(reduceWorkerNumber, System.currentTimeMillis());
            return new ReduceJobDetails(temporaryFileList, totalReduceWorkers, Integer.valueOf(reduceWorkerNumber));
        }
        if (inProgressFiles.size() != 0) {
            String reduceWorkerNumber = inProgressFiles.keySet().iterator().next();
            if ((System.currentTimeMillis() - inProgressFiles.get(reduceWorkerNumber)) / 1000 > 10) {
                inProgressFiles.replace(reduceWorkerNumber, System.currentTimeMillis());
                return new ReduceJobDetails(temporaryFileList, totalReduceWorkers, Integer.valueOf(reduceWorkerNumber));
            } else {
                System.out.println(
                        ".......Don't have reduce jobs right now, Wait for some time if inProgress jobs fails......");
                return null;
            }
        }

        System.out.println("...........Reduce Phase finished.......");

        return null;
    }

    public MapJobDetails getMapJobDetails() {
        if (allFilesQueue.size() != 0) {
            String fileName = allFilesQueue.pollFirst();
            inProgressFiles.put(fileName, System.currentTimeMillis());
            return new MapJobDetails(fileName);
        }
        if (inProgressFiles.size() != 0) {
            String fileName = inProgressFiles.keySet().iterator().next();
            if ((System.currentTimeMillis() - inProgressFiles.get(fileName)) / 1000 > 10) {
                inProgressFiles.replace(fileName, System.currentTimeMillis());
                System.out.println("Seems like worker dead for this file, I am reassigning : " + fileName);
                return new MapJobDetails(fileName);
            } else {
                System.out.println("........Don't have map jobs right now, Wait for Reduce phase....");
                return null;
            }
        }
        System.out.println("...........Map Phase finished.......");

        return null;
    }

    private void prepareForReducePhase() {
        File directory = new File(directoryName);
        String[] allFilesInDirectory = directory.list((dir, name) -> name.startsWith("map-out-"));

        for (String file : allFilesInDirectory) {
            temporaryFileList.add(file);
        }
        for (int i = 0; i < totalReduceWorkers; i = i + 1) {
            allFilesQueue.offerLast(Integer.toString(i));
        }
    }

    private void cleanTemporaryFiles() {
        File directory = new File(directoryName);
        String[] allFilesInDirectory = directory.list((dir, name) -> name.startsWith("map-out-"));

        for (String file : allFilesInDirectory) {
            new File(file).delete();
        }
    }
}