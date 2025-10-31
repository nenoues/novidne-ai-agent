package com.novidne.novidneaiagent.chatmemory.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * 消息序列化器类，使用Kryo库进行对象序列化和反序列化
 * 该类被标记为Spring组件，可以被Spring容器管理
 */
@Component
public class MessageSerializer {
    // 创建Kryo实例，使用static保证全局唯一
    private static final Kryo kryo = new Kryo();

    // 静态初始化块，在类加载时执行
    static {
        // 设置不需要注册类，这样可以序列化任意类
        kryo.setRegistrationRequired(false);
        // 设置标准实例化策略，支持无参构造函数的对象创建
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    /**
     * 将Message对象序列化为Base64编码的字符串
     *
     * @param message 要序列化的消息对象
     * @return Base64编码的字符串
     * @throws RuntimeException 如果序列化过程中发生异常
     */
    public static String serializeMessage(Message message) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Output output = new Output(baos)) {
            // 使用Kryo将消息对象写入输出流
            kryo.writeClassAndObject(output, message);
            // 刷新输出流，确保所有数据都被写入
            output.flush();
            // 将字节数组转换为Base64编码字符串
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            // 抛出运行时异常，包装原始异常信息
            throw new RuntimeException("序列化失败", e);
        }
    }

    /**
     * 将Base64编码的字符串反序列化为Message对象
     *
     * @param base64 Base64编码的字符串
     * @return 反序列化后的Message对象
     * @throws RuntimeException 如果反序列化过程中发生异常
     */
    public static Message deserialize(String base64) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             Input input = new Input(bais)) {
            // 从输入流中读取对象并转换为Message类型
            return (Message) kryo.readClassAndObject(input);
        } catch (Exception e) {
            // 抛出运行时异常，包装原始异常信息
            throw new RuntimeException(e);
        }
    }

}
