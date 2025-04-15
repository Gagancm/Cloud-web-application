package edu.csye6225.neu.webapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HealthCheckService {
    Logger logger = LoggerFactory.getLogger(HealthCheckService.class);

    boolean performHealthCheck();
}