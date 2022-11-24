package Util;
import PreProcessor.Driver;

import java.util.HashMap;
import java.util.logging.Logger;

public class PerformanceTimer {

    // Fields
    private final Logger logger;
    private final HashMap<String, Long> timers;
    private final HashMap<String, Long> records;


    // Constructor
    public PerformanceTimer() {
        this.timers = new HashMap<>();
        this.records = new HashMap<>();
        this.logger = Logger.getLogger(Driver.LOGGER_NAME);
    }


    // Methods
    public void start(String name) {
        this.timers.put(name, System.currentTimeMillis());
    }

    public long stop(String name) {

        if(!this.timers.containsKey(name)) {
            this.logger.info("No timer found with name: " + name);
            return -1;
        }

        long duration = System.currentTimeMillis() - this.timers.get(name);
        this.records.put(name, duration);
        return duration;

    }


    // Getters
    public HashMap<String, Long> getRecords() {
        return this.records;
    }

}
