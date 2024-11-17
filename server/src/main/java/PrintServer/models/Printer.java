package PrintServer.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.Serializable;
import java.util.LinkedList;

public class Printer implements Serializable {
    private final String name;
    private final LinkedList<String> jobsQueue;
    private static final Logger logger = LogManager.getLogger(Printer.class);

    public Printer(String name) {
        this.name = name;
        this.jobsQueue = new LinkedList<>();
    }

    public void topQueue(int job) {
        int index = job - 1;

        // Check if the index is valid
        if (index < 0 || index >= jobsQueue.size()) {
            logger.warn(name + ": Invalid job index: {}", job);
            return;
        }

        String jobToMove = jobsQueue.remove(index);
        jobsQueue.addFirst(jobToMove);
        logger.debug(name + ": Job {} moved to the top of the queue", job);
    }

    public String getjobsQueueAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jobsQueue.size(); i++) {
            sb.append(i + 1).append(". ").append(jobsQueue.get(i)).append("\n");
        }
        return sb.toString();
    }
    public String getStatus() {
        return name + ": " + jobsQueue.size() + " jobs in queue";
    }

    public void print(String filename, String email) {
        logger.info(name + ": Printing " + filename + " for " + email);
    }

    public String getName() {
        return name;
    }


}
