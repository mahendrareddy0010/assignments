package mapReduce.mr;

import java.io.Serializable;
import java.util.List;

public class ReduceJobDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> fileNames;
    private int totalReduceWorkers;
    private int currentReduceNumber;

    // Default constructor
    public ReduceJobDetails() {
    }

    // Parameterized constructor
    public ReduceJobDetails(List<String> fileNames, int totalReduceWorkers, int currentReduceNumber) {
        this.fileNames = fileNames;
        this.totalReduceWorkers = totalReduceWorkers;
        this.currentReduceNumber = currentReduceNumber;
    }

    // Getters and setters
    public List<String> getFileNames() {
        return fileNames;
    }

    public int getTotalReduceWorkers() {
        return totalReduceWorkers;
    }

    public int getCurrentReduceNumber() {
        return currentReduceNumber;
    }

}
