package com.novidne.novidneaiagent.rag;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PlanAppVectorStoreConfig {
    @Resource
    private PlanAppDocumentLoader planAppDocumentLoader;

    @Resource
    private MyKeyWordEnricher myKeyWordEnricher;

    @Bean
    VectorStore planAppVectorStore(DashScopeEmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        List<Document> planDocuments = planAppDocumentLoader.loadMarkdowns();
        List<Document> enrichedDocuments = myKeyWordEnricher.enrichDocuments(planDocuments);
        simpleVectorStore.add(enrichedDocuments);
        return simpleVectorStore;
    }
}
