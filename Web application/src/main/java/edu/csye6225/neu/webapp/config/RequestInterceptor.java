package edu.csye6225.neu.webapp.config;

import edu.csye6225.neu.webapp.service.MetricsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class RequestInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);
    
    private final MetricsService metricsService;
    
    @Autowired
    public RequestInterceptor(MetricsService metricsService) {
        this.metricsService = metricsService;
    }
    
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull Object handler
    ) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? path + "?" + queryString : path;
        
        // Generate a unique request ID and store it in request attribute
        String requestId = "req-" + System.currentTimeMillis() + "-" + Math.random();
        request.setAttribute("requestId", requestId);
        request.setAttribute("startTime", System.currentTimeMillis());
        
        // Log incoming request
        logger.info("[{}] Incoming request: {} {}", requestId, method, fullPath);
        
        // Increment API call counter
        metricsService.incrementApiCounter(method.toLowerCase() + "." + getNormalizedPath(path));
        
        return true;
    }
    
    @Override
    public void postHandle(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull Object handler, 
            ModelAndView modelAndView
    ) {
        // Nothing to do here
    }
    
    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull Object handler, 
            Exception ex
    ) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String requestId = (String) request.getAttribute("requestId");
        Long startTime = (Long) request.getAttribute("startTime");
        
        if (requestId == null || startTime == null) {
            return;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Log completed request
        logger.info("[{}] Request completed: {} {} ({}ms, status: {})", 
                requestId, method, path, duration, response.getStatus());
        
        // Record API execution time
        String apiName = method.toLowerCase() + "." + getNormalizedPath(path);
        metricsService.incrementApiCounter(apiName + ".time");
        
        if (ex != null) {
            logger.error("[{}] Request failed with exception", requestId, ex);
            metricsService.incrementApiCounter(apiName + ".error");
        }
    }
    
    /**
     * Normalize path for metrics by removing variable parts like IDs
     */
    private String getNormalizedPath(String path) {
        // Replace path variables like /resources/123 with /resources/{id}
        return path.replaceAll("/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "/{id}")
                   .replaceAll("/\\d+", "/{id}")
                   .replace('/', '.')
                   .replaceAll("^\\.", ""); // Remove leading dot
    }
}