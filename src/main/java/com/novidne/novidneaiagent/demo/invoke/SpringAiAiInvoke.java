package com.novidne.novidneaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;

/**
 * @author Novidne
 * @date 2025/10/04 22:05
 */
public class SpringAiAiInvoke implements CommandLineRunner {
    /**
     * 注入ChatModel类型的dashscopeChatModel bean
     * 用于与AI模型进行交互
     */
    @Resource
    private ChatModel dashscopeChatModel;

    /**

 * 重写的run方法，用于执行程序主要逻辑
     * @param args 命令行参数
     * @throws Exception 可能抛出的异常
     */
    @Override
    public void run(String... args) throws Exception {
    // 创建一个提示词"你好"，并调用dashscopeChatModel进行对话
        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("你好"))
                .getResult()  // 获取对话结果
                .getOutput(); // 获取输出内容
    // 将助手的回复文本输出到控制台
        System.out.println(assistantMessage.getText());
    }
}
