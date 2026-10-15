package com.reglog.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.reglog.backend.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ReglogBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReglogBackendApplication.class, args);
    }
}
