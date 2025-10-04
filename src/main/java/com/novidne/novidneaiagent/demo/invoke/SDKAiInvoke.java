package com.novidne.novidneaiagent.demo.invoke;// 建议dashscope SDK的版本 >= 2.12.0
import java.util.Arrays;
import java.lang.System;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;

/**
 * @author Novidne
 * @date 2025/10/04 16:31
 */
public class SDKAiInvoke {
    /**

     * 调用模型生成对话内容
     *
     * @return {@link GenerationResult} 模型生成的对话结果
     * @throws ApiException API调用异常
     * @throws NoApiKeyException 未提供API密钥异常
     * @throws InputRequiredException 缺少必要输入参数异常
     */
    public static GenerationResult callWithMessage() throws ApiException, NoApiKeyException, InputRequiredException {
        // 创建Generation实例，用于调用AI模型
        Generation gen = new Generation();
        // 构建系统消息，设定助手角色和行为准则
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())  // 设置系统角色
                .content("You are a helpful assistant.")  // 设置系统提示内容
                .build();
        // 构建用户消息，包含用户的实际输入
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())  // 设置用户角色
                .content("你是谁？")  // 设置用户输入内容
                .build();
        // 构建生成参数，配置API调用的各项设置
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(TestApiKey.API_KEY)  // 从环境变量获取API密钥
                // 此处以qwen-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                .model("qwen-plus")  // 设置使用的模型名称
                .messages(Arrays.asList(systemMsg, userMsg))  // 设置对话消息列表
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)  // 设置返回结果格式为消息格式
                .build();
        // 调用模型并返回结果，执行实际的AI对话生成
        return gen.call(param);
    }

    /**

     * 程序入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 调用AI模型生成对话内容
            GenerationResult result = callWithMessage();
            // 将结果转换为JSON格式并输出
            System.out.println(JsonUtils.toJson(result));
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            // 使用日志框架记录异常信息
            System.err.println("An error occurred while calling the generation service: " + e.getMessage());
        }
        // 退出程序
        System.exit(0);
    }
}