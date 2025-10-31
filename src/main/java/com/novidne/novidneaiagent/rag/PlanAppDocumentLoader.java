package com.novidne.novidneaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PlanAppDocumentLoader {
    private final ResourcePatternResolver resourcePatternResolver;

    public PlanAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载指定路径下的所有Markdown文档文件
     * 该方法会扫描classpath:document/目录下的所有.md文件，并将其转换为Document对象列表
     *
     * @return 包含所有加载的Markdown文档的Document列表，如果加载失败则返回空列表或部分成功的结果
     */
    public List<Document> loadMarkdowns() {
        // 创建一个用于存储文档的列表
        List<Document> planDocuments = new ArrayList<>();
        try {
            // 使用资源模式解析器获取document目录下所有.md文件
            Resource[] planDocumentsResources = resourcePatternResolver.getResources("classpath:document/*.md");
            // 遍历所有找到的Markdown文件资源
            for (Resource planDocumentsResource : planDocumentsResources) {
                // 获取文件名
                String planFileName = planDocumentsResource.getFilename();
                MarkdownDocumentReaderConfig planDocumentReaderConfig = MarkdownDocumentReaderConfig.builder().withHorizontalRuleCreateDocument(true)
                        .withIncludeBlockquote(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("fileName", planFileName)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(planDocumentsResource, planDocumentReaderConfig);
                planDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return planDocuments;
    }
}
