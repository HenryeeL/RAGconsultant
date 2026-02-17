package org.example.consultantrag.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {
    @Bean
    public ChatMemory chatMemory() {
        // 使用持久化存储(需要添加依赖)
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

}
