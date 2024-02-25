package mapReduce.mr;

import java.io.Serializable;

public class MapJobDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;

    // Default constructor
    public MapJobDetails() {
    }

    // Parameterized constructor
    public MapJobDetails(String fileName) {
        this.fileName = fileName;
    }

    // Getters and setters
    public String getFileName() {
        return fileName;
    }

}
