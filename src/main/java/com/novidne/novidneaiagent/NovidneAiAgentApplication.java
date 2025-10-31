package com.novidne.novidneaiagent;

import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * @author Novidne
 * @date 2025/10/04 22:14
 */
@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
@EnableConfigurationProperties
@EnableRedisRepositories
public class NovidneAiAgentApplication {

    /**
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(NovidneAiAgentApplication.class, args);
    }

}
