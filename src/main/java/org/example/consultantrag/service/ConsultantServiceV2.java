package org.example.consultantrag.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

/**
 * 带工具调用能力的顾问服务（V2版本）
 *
 * 新增功能：
 * 1. Function Calling - LLM可以主动调用外部工具
 * 2. 保留所有V1功能（RAG、流式输出、对话记忆）
 *
 * 使用场景：
 * - "北京现在天气怎么样？" -> 自动调用WeatherTool
 * - "计算 123 * 456" -> 自动调用CalculatorTool
 * - "现在几点了？" -> 自动调用TimeTool
 * - "帮我分析一下XX问题" -> 使用RAG检索知识库
 */
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "ollamaChatModel",
        streamingChatModel = "ollamaStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever",
        tools = {"weatherTool", "calculatorTool", "timeTool"}  // 🔧 关键配置：注册工具
)
public interface ConsultantServiceV2 {

    /**
     * 发送消息并获取流式回复（支持工具调用）
     *
     * 工作流程：
     * 1. 用户发送消息
     * 2. LLM分析是否需要调用工具
     * 3. 如果需要，自动调用相应工具并获取结果
     * 4. 将工具结果整合到回复中
     * 5. 流式返回最终答案
     *
     * @param memoryId 会话ID（用于维护上下文）
     * @param message  用户消息
     * @return 响应式流（逐token返回）
     */
    @SystemMessage(fromResource = "system.txt")
    String chat(@MemoryId String memoryId, @UserMessage String message);
}

