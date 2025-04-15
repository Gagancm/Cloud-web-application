package edu.csye6225.neu.webapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration to register request interceptors
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final RequestInterceptor requestInterceptor;
    
    @Autowired
    public WebConfig(RequestInterceptor requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the request interceptor for all paths
        registry.addInterceptor(requestInterceptor);
    }
}