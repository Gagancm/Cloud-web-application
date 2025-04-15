package edu.csye6225.neu.webapp.service;

import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

@Service
public class MetricsService {

    private final StatsDClient statsDClient;

    // Constructor injection
    public MetricsService(StatsDClient statsDClient) {
        this.statsDClient = statsDClient;
    }

    /**
     * Increment a counter for an API endpoint
     * @param apiName The name of the API endpoint
     */
    public void incrementApiCounter(String apiName) {
        statsDClient.incrementCounter("api." + apiName + ".count");
    }

    /**
     * Time the execution of an API call and record it as a timer metric
     * @param apiName The name of the API endpoint
     * @param code The code to execute and time
     * @return The result of the code execution
     */
    public <T> T timeApiExecution(String apiName, Supplier<T> code) {
        Instant start = Instant.now();
        try {
            return code.get();
        } finally {
            Instant end = Instant.now();
            long timeElapsed = Duration.between(start, end).toMillis();
            statsDClient.recordExecutionTime("api." + apiName + ".time", timeElapsed);
        }
    }

    /**
     * Record a database query time directly
     * @param queryName The name of the database query
     * @param timeInMs The time in milliseconds
     */
    public void timeDatabaseQuery(String queryName, long timeInMs) {
        statsDClient.recordExecutionTime("database." + queryName + ".time", timeInMs);
    }

    /**
     * Time the execution of a database query and record it as a timer metric
     * @param queryName The name of the database query
     * @param code The code to execute and time
     * @return The result of the code execution
     */
    public <T> T timeDatabaseQuery(String queryName, Supplier<T> code) {
        Instant start = Instant.now();
        try {
            return code.get();
        } finally {
            Instant end = Instant.now();
            long timeElapsed = Duration.between(start, end).toMillis();
            statsDClient.recordExecutionTime("database." + queryName + ".time", timeElapsed);
        }
    }

    /**
     * Record an S3 operation time directly
     * @param operationName The name of the S3 operation
     * @param timeInMs The time in milliseconds
     */
    public void timeS3Operation(String operationName, long timeInMs) {
        statsDClient.recordExecutionTime("s3." + operationName + ".time", timeInMs);
    }

    /**
     * Time the execution of an S3 operation and record it as a timer metric
     * @param operationName The name of the S3 operation
     * @param code The code to execute and time
     * @return The result of the code execution
     */
    public <T> T timeS3Operation(String operationName, Supplier<T> code) {
        Instant start = Instant.now();
        try {
            return code.get();
        } finally {
            Instant end = Instant.now();
            long timeElapsed = Duration.between(start, end).toMillis();
            statsDClient.recordExecutionTime("s3." + operationName + ".time", timeElapsed);
        }
    }
}