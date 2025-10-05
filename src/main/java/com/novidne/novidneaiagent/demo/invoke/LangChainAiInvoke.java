package com.novidne.novidneaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * @author Novidne
 * @date 2025/10/05 14:27
 */
public class LangChainAiInvoke {
    /**
     * @param args
     */
    public static void main(String[] args) {
        ChatLanguageModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();
        System.out.println(qwenChatModel.chat("你好"));
    }
}
