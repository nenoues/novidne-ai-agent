package com.novidne.novidneaiagent.app;

import com.novidne.novidneaiagent.advisor.MyLoggerAdvisor;
import com.novidne.novidneaiagent.chatmemory.FileBasedChatMemory;
import com.novidne.novidneaiagent.chatmemory.RedisBasedChatMemory;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author Novidne
 * @date 2025/10/06 15:35
 */
@Component
@Slf4j
@Data
public class PlanApp {

    @Autowired
    private final RedisBasedChatMemory redisBasedChatMemory;

    /**
     *
     */
    private final ChatClient chatClient;


    /**
     *
     */
    private static final String SYSTEM_PROMPT = "你是一位经验丰富的「时间安排大师」，擅长通过深度沟通理解用户需求，为其定制贴合实际、容易落地的每日时间规划，帮助用户度过充实且高效的一天。你的核心任务是：像真实场景中的规划顾问一样，通过主动提问层层深入了解用户，基于用户的具体情况生成个性化计划，而非套用固定模板。\n" +
            "\n" +
            "\n" +
            "### 沟通与引导原则：\n" +
            "1. **从基础信息切入**：先温和询问用户的核心作息（如起床/睡觉时间、固定日程如通勤/会议/用餐时段），明确一天的时间框架。  \n" +
            "   例：“今天你大概几点起床呀？有没有固定的事情需要预留时间（比如通勤、固定会议）？”\n" +
            "\n" +
            "2. **聚焦当日目标**：引导用户列出当天想完成的所有目标（工作、学习、生活、健康等均可），不遗漏细节。  \n" +
            "   例：“今天有没有特别想完成的事情？比如工作上的项目、学习任务，或者想抽时间运动、处理家务？可以都告诉我～”\n" +
            "\n" +
            "3. **深挖任务细节**：对每个目标追问关键信息，判断任务量与可行性：  \n" +
            "   - 任务具体内容（如“写报告”是初稿还是修改？“运动”是跑步还是瑜伽？）；  \n" +
            "   - 预估难度/所需专注度（如“需要高度专注”还是“可以碎片化完成”）；  \n" +
            "   - 截止时间/优先级（如“今天必须完成”还是“尽量做”）；  \n" +
            "   - 所需条件（如“需要用电脑”“需要安静环境”）。  \n" +
            "\n" +
            "4. **结合用户习惯调整**：了解用户的高效时段（如“早上脑子更清晰”还是“晚上更专注”）、偏好的任务节奏（如“喜欢集中做完一件事”还是“穿插不同任务”）、容易分心的因素（如“容易拖延”“常有突发消息”），让计划更贴合用户状态。  \n" +
            "   例：“你平时做什么类型的任务效率最高？有没有哪个时间段感觉自己状态最好呀？”\n" +
            "\n" +
            "5. **主动处理不确定性**：若用户对任务时长无概念（如“不知道写一篇短文要多久”），主动提出“可以帮你参考同类任务的平均耗时”（必要时联网查询）；若任务优先级模糊，用“紧急重要四象限”引导用户排序（如“这件事如果今天不做，会有什么影响吗？”）。\n" +
            "\n" +
            "\n" +
            "### 计划生成要求：\n" +
            "1. **优先级排序**：基于用户反馈，明确任务的“必须做”“应该做”“可灵活调整”三类，优先保障“必须做”的时间。  \n" +
            "2. **合理划分时长**：根据任务量、难度、用户效率，拆分具体时段（如“写报告需要2小时，可拆分为上午10-11点+下午3-4点，中间留10分钟休息”），避免过长时段导致疲劳。  \n" +
            "3. **预留缓冲时间**：每天预留20%-30%的弹性时间（如每2小时留10分钟空白），应对突发事项，降低计划崩塌的压力。  \n" +
            "4. **易启动设计**：将复杂任务拆分为“最小行动步骤”（如“不是‘学英语’，而是‘背20个单词+看1篇短文’”），让用户能快速开始。  \n" +
            "5. **贴合场景细节**：若涉及外部场景（如“去超市”“参加活动”），可主动确认或查询相关信息（如“你家到超市大概需要多久？我可以帮你查一下路线耗时”）。\n" +
            "\n" +
            "\n" +
            "### 互动节奏：\n" +
            "始终以“用户反馈→追问补充→调整思路→生成计划”为循环，避免一次性抛出过多问题。在用户提供部分信息后，可先给出初步思路并询问是否合理（如“目前看，你上午9-11点状态好，要不把需要专注的报告放在这段时间？你觉得可行吗？”），再逐步完善。\n" +
            "\n" +
            "记住：你的目标不是给出“完美计划”，而是帮用户找到“自己能坚持的计划”。保持耐心、亲和，让用户感受到你在认真理解他的需求，而非机械输出方案。现在，开始与用户沟通吧～";

    /**
     * 构造函数，用于初始化TimeApp实例
     *
     * @param dashscopeChatModel 聊天模型参数，用于构建ChatClient
     */
    public PlanApp(RedisBasedChatMemory redisBasedChatMemory, ChatModel dashscopeChatModel) {
        this.redisBasedChatMemory = redisBasedChatMemory;
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        FileBasedChatMemory fileBasedChatMemory = new FileBasedChatMemory(fileDir);


        //初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();  // 创建一个基于内存的聊天记忆对象，用于保存对话历史


        // 使用建造者模式构建ChatClient实例
        chatClient = ChatClient.builder(dashscopeChatModel)  // 使用提供的ChatModel作为基础构建器
                .defaultSystem(SYSTEM_PROMPT)  // 设置默认的系统提示语
                .defaultAdvisors(  // 设置默认的顾问列表，用于处理聊天过程中的各种任务
                        new MessageChatMemoryAdvisor(redisBasedChatMemory),  // 消息聊天记忆顾问，用于管理对话记忆
                        new MyLoggerAdvisor()  // 自定义日志顾问，用于记录聊天过程
//                        , new ReReadingAdvisor()  // 重读顾问，用于重新读取和理解对话内容
                )
                .build();  // 完成构建，返回ChatClient实例
    }

    /**
     * 执行聊天方法，根据用户消息和聊天ID获取聊天响应
     * 该方法使用聊天客户端处理用户输入，并利用会话记忆功能保持上下文连贯性
     *
     * @param message 用户输入的消息内容
     * @param chatId  聊天会话的唯一标识符
     * @return {@link String} 返回聊天机器人的响应内容
     */
    public String doChat(String message, String chatId) {
        // 使用chatClient创建聊天请求
        ChatResponse chatResponse = chatClient
                .prompt()  // 开始构建提示
                .user(message)  // 设置用户输入的消息
                // 配置顾问参数，包括会话ID和记忆检索数量
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call().chatResponse();  // 执行调用并获取聊天响应
        // 从响应中提取文本内容
        String content = chatResponse.getResult().getOutput().getText();
        // 记录日志，输出聊天内容
        log.info("content:{}", content);
        // 返回聊天内容
        return content;
    }

    record PlanReport(String title, List<String> plans) {

    }

    /**
     * 执行聊天并生成报告的方法
     * 该方法使用聊天客户端处理用户输入，并返回格式化的计划报告
     *
     * @param message 用户输入的消息内容
     * @param chatId  聊天会话的唯一标识符
     * @return 返回聊天机器人的响应内容，格式为PlanReport对象
     */
    public PlanReport doChatWithReport(String message, String chatId) {
        // 使用chatClient创建聊天请求
        PlanReport planReport = chatClient
                .prompt()  // 开始构建提示
                .system(SYSTEM_PROMPT + "每次对话后都要生成结果，标题为{用户名} {日期}的时间计划安排，内容为计划列表")  // 设置系统提示，包含报告生成要求
                .user(message)  // 设置用户输入的消息
                // 配置顾问参数，包括会话ID和记忆检索数量
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)  // 设置会话ID用于记忆检索
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))  // 设置记忆检索数量为10条
                .call().entity(PlanReport.class);  // 执行调用并将响应转换为PlanReport对象
        // 从响应中提取文本内容
        // 记录日志，输出聊天内容
        log.info("planReport:{}", planReport);  // 记录生成的计划报告内容
        // 返回聊天内容
        return planReport;  // 返回生成的计划报告
    }

    @Resource
    private VectorStore planAppVectorStore;

    @Resource
    private Advisor planAppRagCloudAdvisor;

    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt().user(message).advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)  // 设置会话ID用于记忆检索
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))  // 设置记忆检索数量为10条
                .advisors(new MyLoggerAdvisor())
//                .advisors(planAppRagCloudAdvisor)
                .advisors(new QuestionAnswerAdvisor(planAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}", content);
        return content;
    }


}
