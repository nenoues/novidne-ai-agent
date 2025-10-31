package com.novidne.novidneaiagent.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 Re2 Advisor
 * 可提高大型语言模型的推理能力
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {


    /**
     * 处理建议请求的前置方法，对原始请求进行增强处理
     *
     * @param advisedRequest 原始的建议请求对象
     * @return 处理后的增强建议请求对象
     */
    private AdvisedRequest before(AdvisedRequest advisedRequest) {

        // 创建用户参数的副本，避免修改原始参数
        Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
        // 将用户文本作为re2_input_query参数添加到用户参数中
        advisedUserParams.put("re2_input_query", advisedRequest.userText());

        // 使用构建器模式创建新的AdvisedRequest对象
        // 设置用户文本为模板格式，包含原始查询和重复查询的提示
        // 使用更新后的用户参数构建最终请求
        return AdvisedRequest.from(advisedRequest)
                .userText("""
                        {re2_input_query}
                        Read the question again: {re2_input_query}
                        """)
                .userParams(advisedUserParams)
                .build();
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // 调用链的下一个aroundCall方法，传入处理后的请求
        return chain.nextAroundCall(this.before(advisedRequest));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 调用链的下一个aroundStream方法，传入处理后的请求
        return chain.nextAroundStream(this.before(advisedRequest));
    }

    @Override
    public int getOrder() {
        // 返回顾问的顺序值，0表示最高优先级
        return 0;
    }

    @Override
    public String getName() {
        // 返回当前类的简单名称作为顾问名称
        return this.getClass().getSimpleName();
    }
}

