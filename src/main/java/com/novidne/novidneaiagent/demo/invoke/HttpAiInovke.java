package com.novidne.novidneaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Novidne
 * @date 2025/10/04 16:42
 */
public class HttpAiInovke {
    /**

 * 主函数，用于发送文本生成请求到阿里云DashScope API
     * @param args 命令行参数（本示例中未使用）
     */
    public static void main(String[] args) {
        // 1. 准备请求URL
    // 设置API端点，用于文本生成服务
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 2. 准备请求头
    // 创建Map对象存储HTTP请求头
        Map<String, String> headers = new HashMap<>();
        // 注意：实际使用时替换为你的API Key
    // 从TestApiKey类获取API密钥，用于身份验证
        String apiKey = TestApiKey.API_KEY;
    // 添加授权头，使用Bearer Token认证方式
        headers.put("Authorization", "Bearer " + apiKey);
    // 设置内容类型为JSON
        headers.put("Content-Type", "application/json");

        // 3. 构建请求体JSON
    // 创建请求主体对象
        JSONObject requestBody = new JSONObject();
    // 设置使用的模型为qwen-plus
        requestBody.set("model", "qwen-plus");

        // 构建messages数组
    // 创建消息列表，用于存储对话历史
        List<JSONObject> messages = new ArrayList<>();
        // system消息，定义AI助手的角色和行为
        JSONObject systemMsg = new JSONObject();
        systemMsg.set("role", "system");
        systemMsg.set("content", "You are a helpful assistant.");
        messages.add(systemMsg);

        // user消息，包含用户输入的问题
        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", "你是谁？");
        messages.add(userMsg);

        // 组装input对象
    // 创建输入对象，包含所有消息
        JSONObject input = new JSONObject();
        input.set("messages", messages);
        requestBody.set("input", input);

        // 设置parameters
    // 创建参数对象，配置API调用选项
        JSONObject parameters = new JSONObject();
    // 设置结果格式为message，便于解析
        parameters.set("result_format", "message");
        requestBody.set("parameters", parameters);

        // 4. 发送POST请求
    // 使用HttpRequest发送POST请求到指定URL
        HttpResponse response = HttpRequest.post(url)
                .addHeaders(headers)
                .body(requestBody.toString())
                .execute();

        // 5. 处理响应
    // 检查响应状态
        if (response.isOk()) {
        // 如果请求成功，打印响应结果
            System.out.println("响应结果：" + response.body());
        } else {
        // 如果请求失败，打印错误信息
            System.err.println("请求失败，状态码：" + response.getStatus() + "，响应：" + response.body());
        }
    }
}
