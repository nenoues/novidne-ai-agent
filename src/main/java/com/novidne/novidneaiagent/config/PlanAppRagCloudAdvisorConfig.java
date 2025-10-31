package com.novidne.novidneaiagent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PlanAppRagCloudAdvisorConfig 配置类
 * 用于配置和创建计划应用相关的RAG(检索增强生成)云顾问Bean
 */
@Configuration
@Slf4j
public class PlanAppRagCloudAdvisorConfig {
    // 从配置文件中注入DashScope API密钥
    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    /**
     * 创建并配置PlanAppRagCloudAdvisor Bean
     * 该Bean使用了阿里云DashScope的文档检索功能，基于"时间管理"知识库进行检索增强
     *
     * @return 配置好的Advisor实例，用于提供基于检索增强的顾问服务
     */
    @Bean
    public Advisor planAppRagCloudAdvisor() {
        // 创建DashScopeApi实例，使用配置的API密钥
        DashScopeApi dashScopeApi = new DashScopeApi(dashScopeApiKey);
        // 定义知识库名称
        final String KNOWLEDGE_INDEX = "时间管理";
        // 创建DashScope文档检索器，指定使用"时间管理"知识库
        DashScopeDocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder().withIndexName(KNOWLEDGE_INDEX).build());
        // 构建并返回检索增强顾问
        return RetrievalAugmentationAdvisor.builder().documentRetriever(documentRetriever).build();
    }
}
