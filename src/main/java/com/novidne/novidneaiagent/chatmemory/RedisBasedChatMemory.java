package com.novidne.novidneaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component  // 使用Spring的@Component注解，将此类标记为Spring容器中的一个Bean
public class RedisBasedChatMemory implements ChatMemory {  // 实现ChatMemory接口，基于Redis的聊天记忆实现

    @Autowired  // 自动注入RedisTemplate实例，用于Redis操作
    private RedisTemplate<String, Object> redisTemplate;
    private static final long DEFAULT_EXPIRE_TIME = 7;  // 默认过期时间，单位为天
    private static final String MEMORY_KEY_PREFIX = "novidne:ai:plan:chat:memory:";  // Redis中聊天记忆的键前缀

    // Kryo序列化工具实例，用于对象序列化和反序列化
    private static final Kryo kryo = new Kryo();

    /**
     * 静态初始化块
     * 配置Kryo实例的序列化策略
     */
    static {
        kryo.setRegistrationRequired(false);  // 设置Kryo不需要注册类
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());  // 设置标准实例化策略
    }

    @Override  // 重写接口方法，添加单条消息到对话记忆中
    public void add(String conversationId, Message message) {
        String key = MEMORY_KEY_PREFIX + conversationId;  // 构建Redis键
        List<Message> messages = getMessages(key);  // 获取现有消息列表
        messages.add(message);  // 添加新消息
        saveMessages(key, messages);  // 保存更新后的消息列表
    }

    @Override  // 重写接口方法，批量添加消息到对话记忆中
    public void add(String conversationId, List<Message> messages) {
        String key = MEMORY_KEY_PREFIX + conversationId;  // 构建Redis键
        List<Message> existingMessages = getMessages(key);  // 获取现有消息列表
        existingMessages.addAll(messages);  // 添加批量消息
        saveMessages(key, existingMessages);  // 保存更新后的消息列表
    }

    @Override  // 重写接口方法，获取对话的最后N条消息
    public List<Message> get(String conversationId, int lastN) {
        String key = MEMORY_KEY_PREFIX + conversationId;  // 构建Redis键
        List<Message> messages = getMessages(key);  // 获取消息列表
        int size = messages.size();  // 获取消息列表大小
        return messages.subList(Math.max(0, size - lastN), size);  // 返回最后N条消息
    }

    @Override  // 重写接口方法，清除指定对话的记忆
    public void clear(String conversationId) {
        String key = MEMORY_KEY_PREFIX + conversationId;  // 构建Redis键
        redisTemplate.delete(key);  // 从Redis中删除该键
    }

    /**
     * 将消息列表保存到Redis中
     *
     * @param key      Redis的键
     * @param messages 要保存的消息列表
     */
    private void saveMessages(String key, List<Message> messages) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();  // 创建字节数组输出流，用于存储序列化后的数据
             Output output = new Output(baos)) {  // 创建Kryo的Output对象，用于将数据写入字节数组输出流
            kryo.writeObject(output, messages);  // 使用Kryo将消息列表对象序列化并写入输出流
            /**
             * 必须注意，如果不添加这一行，那么无法将字节流写入redis中
             * com.esotericsoftware.kryo.io.KryoBufferUnderflowException: Buffer underflow.（表示读取了本不应为空的数据导致缓冲区数据溢出）*/
            output.flush();
            redisTemplate.opsForValue().set(key, baos.toByteArray(), DEFAULT_EXPIRE_TIME, TimeUnit.DAYS);  // 将序列化后的数据存入Redis，设置过期时间
        } catch (Exception e) {
            throw new RuntimeException("Failed to save messages to Redis", e);  // 抛出运行时异常
        }
    }

    /**
     * 从Redis中获取消息列表
     *
     * @param key Redis的键
     * @return 消息列表，如果不存在则返回空列表
     */
    private List<Message> getMessages(String key) {
        byte[] messagesFromRedis = (byte[]) redisTemplate.opsForValue().get(key);  // 从Redis获取字节数组
        if (messagesFromRedis == null) {  // 如果数据不存在
            return new ArrayList<>();  // 返回空列表
        }
        Input input = new Input(new ByteArrayInputStream(messagesFromRedis));  // 创建Kryo输入流
        return kryo.readObject(input, ArrayList.class);  // 使用Kryo反序列化并返回消息列表
    }
}
