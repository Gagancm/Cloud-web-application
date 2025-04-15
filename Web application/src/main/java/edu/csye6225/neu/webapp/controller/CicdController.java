package edu.csye6225.neu.webapp.controller;

import edu.csye6225.neu.webapp.service.HealthCheckService;
import edu.csye6225.neu.webapp.service.MetricsService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

@RestController
public class CicdController {
    private static final Logger logger = LoggerFactory.getLogger(CicdController.class);

    private final HealthCheckService healthCheckService;
    private final MetricsService metricsService;

    @Autowired
    public CicdController(HealthCheckService healthCheckService, MetricsService metricsService) {
        this.healthCheckService = healthCheckService;
        this.metricsService = metricsService;
    }

    private HttpHeaders getSecurityHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.set("X-Content-Type-Options", "nosniff");
        return headers;
    }

    @GetMapping("/cicd")
    public ResponseEntity<Void> cicdEndpoint(HttpServletRequest request) {
        logger.info("CICD endpoint request received");
        metricsService.incrementApiCounter("cicd_endpoint");
        
        return metricsService.timeApiExecution("cicd_endpoint", () -> {
            HttpHeaders headers = getSecurityHeaders();

            if (request.getContentLength() > 0 || !request.getParameterMap().isEmpty()) {
                logger.warn("CICD endpoint rejected due to request body or parameters");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .headers(headers)
                        .build();
            }

            boolean isHealthy = healthCheckService.performHealthCheck();
            logger.info("CICD endpoint health check result: {}", isHealthy ? "healthy" : "unhealthy");

            return isHealthy ? ResponseEntity.ok().headers(headers).build()
                    : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).headers(headers).build();
        });
    }

    @RequestMapping(value = "/cicd", method = {
            RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
            RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS,
            RequestMethod.TRACE
    })
    public ResponseEntity<Void> methodNotAllowed() {
        logger.warn("Unsupported HTTP method attempted on /cicd path");
        metricsService.incrementApiCounter("cicd_endpoint_method_not_allowed");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(getSecurityHeaders())
                .build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Void> handleMethodNotAllowed() {
        logger.warn("Method not allowed exception thrown");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(getSecurityHeaders())
                .build();
    }
}