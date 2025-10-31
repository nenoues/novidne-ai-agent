package com.novidne.novidneaiagent.config;

import com.novidne.novidneaiagent.rag.PlanAppDocumentLoader;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Configuration
public class PgVectorStoreConfig {


    @Resource
    private PlanAppDocumentLoader planAppDocumentLoader;

    /**
     * 创建并配置一个基于PostgreSQL的向量存储Bean
     *
     * @param jdbcTemplate            Spring的JdbcTemplate模板，用于数据库操作
     * @param dashscopeEmbeddingModel Dashscope的嵌入模型，用于向量生成
     * @return 配置好的VectorStore实例
     */
    @Bean  // 声明该方法为一个Spring Bean，返回的对象将被纳入Spring容器管理
    public VectorStore pgVectorStore(JdbcTemplate jdbcTemplate, @Qualifier("dashscopeEmbeddingModel") EmbeddingModel dashscopeEmbeddingModel) {
        // 使用PgVectorStore的构建器模式创建向量存储实例
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)  // 设置距离类型为余弦距离
                .indexType(PgVectorStore.PgIndexType.HNSW)  // 设置索引类型为HNSW（分层小世界图）
                .initializeSchema(true)  // 初始化数据库架构
                .schemaName("public")  // 指定数据库模式名称
                .vectorTableName("plan_app_vector_store")  // 指定向量存储表的名称
                .maxDocumentBatchSize(10000)  // 设置最大文档批处理大小
                .build();  // 构建向量存储实例
        // 从文档加载器中加载Markdown文档
        List<Document> documents = planAppDocumentLoader.loadMarkdowns();
        // 将加载的文档添加到向量存储中
        vectorStore.add(documents);
        // 返回配置完成的向量存储实例
        return vectorStore;
    }
}
