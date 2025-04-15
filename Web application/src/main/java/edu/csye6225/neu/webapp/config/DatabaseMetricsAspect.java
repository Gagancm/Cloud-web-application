package edu.csye6225.neu.webapp.config;

import edu.csye6225.neu.webapp.service.MetricsService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for automatically measuring database operation times
 * using Spring AOP (Aspect Oriented Programming)
 */
@Aspect
@Component
public class DatabaseMetricsAspect {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMetricsAspect.class);
    
    private final MetricsService metricsService;
    
    public DatabaseMetricsAspect(MetricsService metricsService) {
        this.metricsService = metricsService;
    }
    
    /**
     * Pointcut for repository methods
     */
    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public void repositoryMethods() {}
    
    /**
     * Advice that measures database operation time
     */
    @Around("repositoryMethods()")
    public Object measureDatabaseOperationTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String operationName = className + "." + methodName;
        
        logger.debug("Measuring repository operation: {}", operationName);
        
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            // Use the direct timing method instead of the Supplier version
            metricsService.timeDatabaseQuery("db." + operationName, duration);
            logger.debug("Repository operation {} took {}ms", operationName, duration);
        }
    }
}