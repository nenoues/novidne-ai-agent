package com.novidne.novidneaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class OllamaAiInovkeTest {

    @Resource
    private ChatModel ollamaChatModel;

    @Test
    void run() {
        System.out.println(ollamaChatModel.call(new Prompt("你好")).getResult().getOutput());
    }
}