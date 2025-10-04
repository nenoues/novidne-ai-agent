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
     * @param args
     */
    public static void main(String[] args) {
        // 1. 准备请求URL
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 2. 准备请求头
        Map<String, String> headers = new HashMap<>();
        // 注意：实际使用时替换为你的API Key
        String apiKey = TestApiKey.API_KEY;
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Content-Type", "application/json");

        // 3. 构建请求体JSON
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "qwen-plus");

        // 构建messages数组
        List<JSONObject> messages = new ArrayList<>();
        // system消息
        JSONObject systemMsg = new JSONObject();
        systemMsg.set("role", "system");
        systemMsg.set("content", "You are a helpful assistant.");
        messages.add(systemMsg);

        // user消息
        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", "你是谁？");
        messages.add(userMsg);

        // 组装input对象
        JSONObject input = new JSONObject();
        input.set("messages", messages);
        requestBody.set("input", input);

        // 设置parameters
        JSONObject parameters = new JSONObject();
        parameters.set("result_format", "message");
        requestBody.set("parameters", parameters);

        // 4. 发送POST请求
        HttpResponse response = HttpRequest.post(url)
                .addHeaders(headers)
                .body(requestBody.toString())
                .execute();

        // 5. 处理响应
        if (response.isOk()) {
            System.out.println("响应结果：" + response.body());
        } else {
            System.err.println("请求失败，状态码：" + response.getStatus() + "，响应：" + response.body());
        }
    }
}
