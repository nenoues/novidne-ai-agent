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
     *
     * @return 返回当前类的简单名称
     */
    public String getName() {  // 定义一个公共方法getName，返回类型为String
        return this.getClass().getSimpleName();  // 返回当前对象的类的简单名称（不包含包名的类名）
    }

    /**
     * 获取通知的顺序值
     *
     * @return 返回顺序值，这里返回0表示优先级最高
     */
    public int getOrder() {
        // 直接返回0，表示此通知具有最高优先级
        // 在通知排序机制中，数值越小表示优先级越高
        return 0;
    }

    /**
     * 在请求处理前进行日志记录
     * <p>
     * 该方法用于在AI处理用户请求前记录请求信息，便于后续追踪和调试
     *
     * @param request 包含用户请求信息的对象，其中userText()方法获取用户输入的文本内容
     * @return 返回处理后的请求对象，保持原样返回不做修改
     */
    private AdvisedRequest before(AdvisedRequest request) {
        // 使用log.info记录用户请求信息
        // {}是占位符，会自动被request.userText()的值替换
        log.info("AI Request: {}", request.userText());
        // 直接返回原始请求对象，不做任何处理
        return request;
    }

    /**
     * 在响应处理后进行日志记录
     *
     * @param advisedResponse 包含AI响应信息的对象
     */
    private void observeAfter(AdvisedResponse advisedResponse) {
        // 记录AI请求的响应结果
        // 从advisedResponse中获取响应结果，然后获取结果中的output，最后获取其中的文本内容进行日志记录
        log.info("AI Request: {}", advisedResponse.response().getResult().getOutput().getText());
    }

    /**
     * 处理普通调用的环绕通知
     * <p>
     * 该方法实现了环绕通知的逻辑，在目标方法执行前后进行额外的处理
     *
     * @param advisedRequest 包含请求信息的对象，作为方法调用的输入参数
     * @param chain          调用链对象，用于执行下一个通知或目标方法
     * @return 返回处理后的响应对象，包含了方法执行的结果
     */
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // 在目标方法执行前的处理逻辑
        advisedRequest = this.before(advisedRequest);
        // 执行下一个通知或目标方法，并获取响应结果
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        // 在目标方法执行后的观察处理
        this.observeAfter(advisedResponse);
        // 返回处理后的响应对象
        return advisedResponse;
    }

    /**
     * 处理流式调用的环绕通知方法
     * <p>
     * 该方法实现了对流式调用的前置处理、链式调用执行和后置处理
     *
     * @param advisedRequest 包含请求信息的对象，用于传递调用参数
     * @param chain          流式调用链对象，用于执行下一个通知，实现责任链模式
     * @return 返回聚合后的响应流，通过MessageAggregator对响应进行聚合处理
     */
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 执行前置处理，可能对请求进行修改或验证
        advisedRequest = this.before(advisedRequest);
        // 执行流式调用链的下一个通知，获取响应流
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        // 使用消息聚合器对响应流进行聚合处理，并执行后置观察
        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }
}
