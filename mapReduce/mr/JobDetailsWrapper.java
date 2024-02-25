package mapReduce.mr;

import java.io.Serializable;

public class JobDetailsWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    private Object jobDetails;

    // Default constructor
    public JobDetailsWrapper() {
    }

    // Parameterized constructor
    public JobDetailsWrapper(Object jobDetails) {
        this.jobDetails = jobDetails;
    }

    public Object getJobDetails() {
        return jobDetails;
    }

}
