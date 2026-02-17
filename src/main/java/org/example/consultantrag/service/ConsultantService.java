package org.example.consultantrag.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "ollamaChatModel",
        streamingChatModel = "ollamaStreamingChatModel",
        chatMemory = "chatMemory"
)
public interface ConsultantService {

    /**
     * 发送消息并获取流式回复
     * @param message 用户消息
     * @return 响应式流（逐 token 返回）
     */
    @SystemMessage("你的名字是kk")
    Flux<String> chat(String message);
}