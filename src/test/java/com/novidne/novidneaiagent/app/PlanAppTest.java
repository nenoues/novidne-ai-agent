package com.novidne.novidneaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class PlanAppTest {
    @Resource
    private PlanApp planApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是Novidne";
        String answer = planApp.doChat(message, chatId);

        message = "我想更好安排时间";
        answer = planApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        message = "我是谁来着，帮我回忆一下";
        answer = planApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是Novidne";
        PlanApp.PlanReport planReport = planApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(planReport);
    }
}