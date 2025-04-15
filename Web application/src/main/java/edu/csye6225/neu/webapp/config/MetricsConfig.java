package edu.csye6225.neu.webapp.config;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Value("${spring.application.name:web-app}")
    private String applicationName;

    @Bean
    public StatsDClient statsDClient() {
        return new NonBlockingStatsDClient(
                "csye6225",      // prefix to use with metrics
                "localhost",      // StatsD host
                8125              // StatsD port
        );
    }
}