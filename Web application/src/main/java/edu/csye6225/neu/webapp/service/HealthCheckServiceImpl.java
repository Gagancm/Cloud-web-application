package edu.csye6225.neu.webapp.service;

import edu.csye6225.neu.webapp.entity.HealthCheck;
import edu.csye6225.neu.webapp.repository.HealthCheckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckServiceImpl implements HealthCheckService {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckServiceImpl.class);

    @Autowired
    private HealthCheckRepository healthCheckRepository;
    
    @Autowired
    private MetricsService metricsService;

    @Override
    public boolean performHealthCheck() {
        logger.info("Starting health check operation");
        try {
            // Use metrics service to time the database operation
            HealthCheck healthCheck = metricsService.timeDatabaseQuery("healthCheck", () -> {
                HealthCheck check = new HealthCheck();
                return healthCheckRepository.save(check);
            });
            
            logger.info("Health check completed successfully");
            return true;
        } catch (Exception e) {
            logger.error("Health check operation failed", e);
            return false;
        }
    }
}