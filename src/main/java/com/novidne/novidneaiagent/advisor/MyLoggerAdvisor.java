//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.novidne.novidneaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * MyLoggerAdvisor 类实现了一个日志记录功能，用于在AI请求和响应过程中记录相关信息。
 * 该类实现了 CallAroundAdvisor 和 StreamAroundAdvisor 接口，提供了环绕通知的功能。
 */
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    /**
     * 获取当前类的简单名称
     * @return 返回当前类的简单名称
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取通知的顺序值
     * @return 返回顺序值，这里返回0表示优先级最高
     */
    public int getOrder() {
        return 0;
    }

    /**
     * 在请求处理前进行日志记录
     * @param request 包含用户请求信息的对象
     * @return 返回处理后的请求对象
     */
    private AdvisedRequest before(AdvisedRequest request) {
        log.info("AI Request: {}", request.userText());
        return request;
    }

    /**
     * 在响应处理后进行日志记录
     * @param advisedResponse 包含AI响应信息的对象
     */
    private void observeAfter(AdvisedResponse advisedResponse) {
        log.info("AI Request: {}", advisedResponse.response().getResult().getOutput().getText());
    }

    /**
     * 处理普通调用的环绕通知
     * @param advisedRequest 包含请求信息的对象
     * @param chain 调用链对象，用于执行下一个通知
     * @return 返回处理后的响应对象
     */
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        this.observeAfter(advisedResponse);
        return advisedResponse;
    }

    /**
     * 处理流式调用的环绕通知
     * @param advisedRequest 包含请求信息的对象
     * @param chain 流式调用链对象，用于执行下一个通知
     * @return 返回聚合后的响应流
     */
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }
}
