package org.example.consultantrag.repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {
    //注入Redis Template
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        //获取对话消息
        String json = stringRedisTemplate.opsForValue().get(memoryId);
        //把json字符串转换成List<ChatMessage>
        List<ChatMessage> list = ChatMessageDeserializer.messagesFromJson(json);
        return list;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        //更新会话消息
        //1.把list转换成json数据
        String json = ChatMessageSerializer.messagesToJson(list);
        //2.把json数据存储到redis
        stringRedisTemplate.opsForValue().set(memoryId.toString(), json, Duration.ofDays(1));

    }

    @Override
    public void deleteMessages(Object memoryId) {
        stringRedisTemplate.delete(memoryId.toString());
    }
}
