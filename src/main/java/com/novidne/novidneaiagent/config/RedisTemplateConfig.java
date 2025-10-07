package com.novidne.novidneaiagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration // 标记该类为配置类，用于替代XML配置文件
public class RedisTemplateConfig {


    /**
     * redis模板配置类
     * 用于配置RedisTemplate实例，设置序列化方式和连接工厂
     *
     * @param redisConnectionFactory Redis连接工厂，用于创建Redis连接
     * @return {@link RedisTemplate<String, Object>}
     * @author CodeGeeX
     */
    @Bean // 将方法的返回值对象作为Spring容器中的一个Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 创建RedisTemplate实例
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 创建字符串序列化器
        RedisSerializer<String> stringRedisSerializer = RedisSerializer.string();
        // 设置key的序列化方式为字符串序列化器
        redisTemplate.setKeySerializer(stringRedisSerializer);
        // 设置hash key的序列化方式为字符串序列化器
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 返回配置好的RedisTemplate实例
        return redisTemplate;
    }

//    @Bean
//    public RedisTemplate<String, byte[]> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//        // 设置序列化器
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new RedisSerializer<byte[]>() {
//            @Override
//            public byte[] serialize(byte[] bytes) throws SerializationException {
//                return bytes;
//            }
//
//            @Override
//            public byte[] deserialize(byte[] bytes) throws SerializationException {
//                return bytes;
//            }
//        });
//        template.afterPropertiesSet();
//        return template;
//    }
}
