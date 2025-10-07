package com.novidne.novidneaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * @author Novidne
 * @date 2025/10/05 14:27
 */
public class LangChainAiInvoke {
    /**

 * 主方法入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
    // 创建QwenChatModel实例，使用builder模式构建
    // 设置API密钥和模型名称为qwen-max
        ChatLanguageModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)  // 使用测试API密钥
                .modelName("qwen-max")        // 指定使用qwen-max模型
                .build();                     // 构建模型实例
    // 调用模型的chat方法，传入"你好"作为消息，并打印返回结果
        System.out.println(qwenChatModel.chat("你好"));
    }
}
