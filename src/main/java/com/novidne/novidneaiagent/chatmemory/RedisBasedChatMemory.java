package com.novidne.novidneaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.novidne.novidneaiagent.chatmemory.serializer.MessageSerializer;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component  // 使用Spring的@Component注解，将此类标记为Spring容器中的一个Bean
@Slf4j
public class RedisBasedChatMemory implements ChatMemory {  // 实现ChatMemory接口，基于Redis的聊天记忆实现

    @Autowired  // 自动注入RedisTemplate实例，用于Redis操作
    private RedisTemplate<String, Object> redisTemplate;
    private static final long DEFAULT_EXPIRE_TIME = 7;  // 默认过期时间，单位为天
    private static final String MEMORY_KEY_PREFIX = "novidne:ai:plan:chat:memory:";  // Redis中聊天记忆的键前缀


    @Override  // 重写接口方法，添加单条消息到对话记忆中
    public void add(String conversationId, Message message) {
        String key = MEMORY_KEY_PREFIX + conversationId;  // 构建Redis键
        List<Message> messages = getMessagesFromRedis(key);  // 获取现有消息列表
        messages.add(message);  // 添加新消息
        saveMessages(key, messages);  // 保存更新后的消息列表
    }

    @Override  // 重写接口方法，批量添加消息到对话记忆中
    public void add(String conversationId, List<Message> messages) {
        String key = MEMORY_KEY_PREFIX + conversationId;  // 构建Redis键
        List<Message> existingMessages = getMessagesFromRedis(key);  // 获取现有消息列表
        existingMessages.addAll(messages);  // 添加批量消息
        saveMessages(key, existingMessages);  // 保存更新后的消息列表
    }

    @Override  // 重写接口方法，获取对话的最后N条消息
    public List<Message> get(String conversationId, int lastN) {
        String key = MEMORY_KEY_PREFIX + conversationId;  // 构建Redis键
        List<Message> messages = getMessagesFromRedis(key);  // 获取消息列表
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
        List<String> serializedMessages = new ArrayList<>(messages.size());
        for (Message message : messages) {
            try {
                String serializedMessage = MessageSerializer.serializeMessage(message);
                serializedMessages.add(serializedMessage);
            } catch (Exception e) {
                log.error("序列化消息失败,跳过该消息：{}", message, e);
            }
        }
        redisTemplate.opsForValue().set(key, serializedMessages, DEFAULT_EXPIRE_TIME, TimeUnit.DAYS);  // 保存消息列表到Redis，并设置过期时间
    }

    /**
     * 从Redis中获取消息列表
     *
     * @param key Redis的键
     * @return 消息列表，如果不存在则返回空列表
     */
    private List<Message> getMessagesFromRedis(String key) {
        Object value = redisTemplate.opsForValue().get(key);// 从Redis获取字节数组
        if (value == null) {
            return new ArrayList<>();
        }
        if (!(value instanceof List)) {
            log.error("Redis中存储的值不是List类型，实际为：{}", value.getClass().getName());
            return new ArrayList<>();
        }
        List<String> serializedMessages = new ArrayList<>();
        for (Object obj : (List<?>) value) {
            if (obj instanceof String) {
                serializedMessages.add((String) obj);
            } else {
                log.warn("Redis中存储的值不是String类型，实际为：{}", obj.getClass().getName());
            }
        }
        List<Message> messagesFromRedis = new ArrayList<>(serializedMessages.size());
        for (String serializedMessage : serializedMessages) {
            try {
                Message message = MessageSerializer.deserialize(serializedMessage);
                messagesFromRedis.add(message);
            } catch (Exception e) {
                log.error("反序列化消息失败，跳过该消息：{}", serializedMessage, e);
            }
        }
        return messagesFromRedis;
    }
}
