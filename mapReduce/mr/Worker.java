package mapReduce.mr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mapReduce.mrApplication.MapReduce;

public class Worker {
    MapReduce mapReduce;
    static final Random random = new Random();

    public Worker(MapReduce mapReduce, CoordinateRPCInterface remoteObj) {
        this.mapReduce = mapReduce;
        start(remoteObj);
    }

    private void start(CoordinateRPCInterface remoteObj) {
        while (true) {
            try {
                JobDetailsWrapper jobDetailsWrapper = remoteObj.getJob();
                if (jobDetailsWrapper == null) {
                    System.out.println("I did not get job. So, Waiting for 1 sec");
                    Thread.sleep(1000);
                    continue;
                }
                Object jobDetails = jobDetailsWrapper.getJobDetails();
                if (jobDetails instanceof MapJobDetails) {
                    doMapPhaseJob((MapJobDetails) jobDetails);
                    remoteObj.notifyMapJobFinished(((MapJobDetails) jobDetails).getFileName());
                } else {
                    doReducePhaseJob((ReduceJobDetails) jobDetails);
                    remoteObj.notifyReduceJobFinished(((ReduceJobDetails) jobDetails).getCurrentReduceNumber());
                }
            } catch (Exception e) {
                System.out.println("Worker unable to contact Coordinator remote object or Some I/O exception ");
                System.out.println("Exception : " + e);
                System.out.println(".......Exiting .........");
                break;
            }

        }
    }

    private void doMapPhaseJob(MapJobDetails mapJobDetails) throws IOException {
        String fileName = mapJobDetails.getFileName();
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
        List<KeyValue> keyValues = mapReduce.map(content);

        String outputFIleName = "map-out-" + fileName;
        AtomicFile atomicFile = new AtomicFile("temp-map-out-", ".txt", outputFIleName);
        for (KeyValue keyValue : keyValues) {
            if (keyValue.key == "to" && keyValue.value == "") {
                System.out.println("something is wrong");
            }
            atomicFile.write(keyValue.key + " " + keyValue.value + "\n");
        }
        atomicFile.close();
    }

    private void doReducePhaseJob(ReduceJobDetails jobDetails) {
        Map<String, List<String>> keyValues = new HashMap<>();
        try {
            for (String fileName : jobDetails.getFileNames()) {
                String content = new String(Files.readAllBytes(Paths.get(fileName)));
                for (String line : content.split("\n")) {
                    String[] words = line.split(" ");
                    if (words.length != 2) {
                        System.out.println(line);
                        continue;
                    }
                    String key = words[0];
                    String value = words[1];
                    if (ihash(key) % jobDetails.getTotalReduceWorkers() == jobDetails.getCurrentReduceNumber()) {
                        if (!keyValues.containsKey(key)) {
                            keyValues.put(key, new ArrayList<>());
                        }
                        keyValues.get(key).add(value);
                    }
                }
            }

            String outputFIleName = "mr-out-" + jobDetails.getCurrentReduceNumber() + ".txt";
            AtomicFile atomicFile = new AtomicFile("temp-reduce-out-", ".txt", outputFIleName);
            for (String key : keyValues.keySet()) {
                atomicFile.write(key + " " + mapReduce.reduce(key, keyValues.get(key)) + "\n");
            }
            atomicFile.close();
        } catch (IOException e) {
            System.out.println("");
        }

    }

    private int ihash(String word) {
        // return word.hashCode();
        // -------- OR ------------
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(word.getBytes());
            // Convert bytes to integer
            int hash = 0;
            for (byte b : hashBytes) {
                hash = (hash << 8) | (b & 0xFF);
            }
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return 0; // Handle error
        }
    }
}
