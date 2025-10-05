package com.novidne.novidneaiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author Novidne
 * @date 2025/10/04 22:14
 */
@SpringBootApplication
@EnableConfigurationProperties
public class NovidneAiAgentApplication {

    /**
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(NovidneAiAgentApplication.class, args);
    }

}
