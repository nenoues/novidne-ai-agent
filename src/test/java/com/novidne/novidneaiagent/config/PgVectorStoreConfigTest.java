package com.novidne.novidneaiagent.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PgVectorStoreConfigTest {
    @Resource
    VectorStore pgVectorStore;

    @Test
    void test() {
        List<Document> documents = List.of(new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                //public Document(@JsonProperty("content") String content)
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                //public Document(String text, Map<String, Object> metadata) {
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
        pgVectorStore.add(documents);
        List<Document> results = pgVectorStore.similaritySearch(SearchRequest.builder().query("String").topK(5).build());
        Assertions.assertNotNull(results);

    }
}