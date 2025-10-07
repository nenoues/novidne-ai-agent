package com.novidne.novidneaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件的聊天记忆存储系统实现类
 * 使用文件系统持久化存储聊天消息记录
 */
public class FileBasedChatMemory implements ChatMemory {

    // 存储聊天记录的基础目录路径
    private final String BASE_DIR;
    // Kryo序列化工具实例，用于对象序列化和反序列化
    private static final Kryo kryo = new Kryo();

    /**
     * 静态初始化块
     * 配置Kryo实例的序列化策略
     */
    static {
        // 设置不需要注册类即可序列化
        kryo.setRegistrationRequired(false);
        //设置实例化策略为标准实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    /**
     * 构造函数：初始化基于文件的聊天记忆存储系统
     *
     * @param dir 用于存储聊天记录的目录路径
     */
    public FileBasedChatMemory(String dir) {
        // 初始化基础目录路径
        BASE_DIR = dir;
        // 创建文件对象，表示基础目录
        File baseDirFile = new File(dir);
        // 检查目录是否存在
        if (!baseDirFile.exists()) {
            // 如果目录不存在，则创建所有必要的父目录
            baseDirFile.mkdirs();
        }
    }

    @Override    // 重写父类或接口的方法
    /**
     * 向指定对话中添加单条消息
     * @param conversationId 对话ID，用于标识特定的对话
     * @param message 要添加的消息对象
     */
    public void add(String conversationId, Message message) {    // 添加消息到会话的方法
        List<Message> conversationMessage = getOrderCreateConversation(conversationId);// 调用保存会话的方法，将单个消息包装为列表后保存
        conversationMessage.add(message);
        saveConversation(conversationId, conversationMessage);
    }

    @Override
    /**
     * 向指定对话中添加消息列表
     * @param conversationId 对话ID，用于标识特定的对话
     * @param messages 要添加的消息列表，包含一条或多条消息内容
     */
    public void add(String conversationId, List<Message> messages) {
        // 获取指定对话的现有消息列表
        List<Message> messageList = getOrderCreateConversation(conversationId);
        // 将新消息添加到现有消息列表中
        messageList.addAll(messages);
        // 保存更新后的对话消息列表
        saveConversation(conversationId, messageList);
    }

    @Override  // 重写父类或接口中的方法
    /**
     * 根据会话ID获取最近N条消息
     * @param conversationId 会话ID，用于标识特定的对话
     * @param lastN 需要获取的消息数量，表示从最近的消息开始算起
     * @return 返回包含最近N条消息的列表，如果消息数量不足N条，则返回所有可用消息
     */
    public List<Message> get(String conversationId, int lastN) {
        // 获取指定会话的所有消息并按顺序存储在列表中
        List<Message> messageList = getOrderCreateConversation(conversationId);
        // 使用subList方法获取列表中从指定位置到末尾的子列表
        // Math.max确保当lastN大于消息列表大小时，从0开始截取，避免IndexOutOfBoundsException
        return messageList.stream()  // 创建消息列表的流
                .skip(Math.max(0, messageList.size() - lastN))  // 跳过前size-lastN条消息，保留最近的N条
                .toList();  // 将流转换为列表返回
    }

    @Override
    /**
     * 清除指定对话的所有数据
     * @param conversationId 对话的唯一标识符
     */
    public void clear(String conversationId) {
        // 根据对话ID获取对应的文件
        File conversationFile = getConversationFile(conversationId);
        // 检查文件是否存在
        if (conversationFile.exists()) {
            // 如果文件存在，则删除该文件
            conversationFile.delete();
        }
    }

    /**
     * 根据对话ID获取对应的对话文件
     *
     * @param conversationId 对话的唯一标识符
     * @return 返回一个File对象，表示存储对话内容的文件
     */
    private File getConversationFile(String conversationId) {
        // 创建并返回一个File对象，文件路径为BASE_DIR目录下，文件名为conversationId加上.kryo后缀
        return new File(BASE_DIR, conversationId + ".kryo");
    }

    /**
     * 根据会话ID获取创建订单的会话消息列表
     *
     * @param conversationId 会话ID，用于标识特定的会话
     * @return 返回消息列表，如果文件不存在或读取失败则返回空列表
     */
    private List<Message> getOrderCreateConversation(String conversationId) {
        // 根据会话ID获取对应的会话文件
        File conversationFile = getConversationFile(conversationId);
        // 创建消息列表对象
        List<Message> messages = new ArrayList<>();
        // 检查会话文件是否存在
        if (conversationFile.exists()) {
            try (Input input = new Input(new FileInputStream(conversationFile))) {
                // 使用Kryo库从文件中读取消息列表
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                // 捕获并打印IO异常
                e.printStackTrace();
            }
        }
        // 返回消息列表
        return messages;
    }

    /**
     * 保存会话消息到本地文件
     *
     * @param conversationId 会话的唯一标识ID
     * @param messages       需要保存的消息列表
     */
    private void saveConversation(String conversationId, List<Message> messages) {

        // 根据会话ID获取对应的会话文件
        File conversationFile = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(conversationFile))) {
            // 使用Kryo库将消息列表写入文件
            kryo.writeObject(output, messages);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
