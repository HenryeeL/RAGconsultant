package org.example.consultantrag.agent;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ReAct Agent 实现（暂不启用，仅作参考）
 *
 * ReAct = Reasoning + Acting
 *
 * 工作流程：
 * 1. Thought - 分析当前需要做什么
 * 2. Action - 决定调用哪个工具
 * 3. Observation - 观察工具返回结果
 * 4. 重复1-3直到任务完成
 * 5. Answer - 给出最终答案
 *
 * 使用场景：
 * - 复杂任务需要多步骤规划
 * - 需要根据中间结果调整策略
 * - 多个工具需要协同工作
 *
 * 示例任务：
 * "帮我查询北京的天气，如果温度超过25度，计算比20度高多少"
 *
 * Agent执行过程：
 * 1. Thought: 需要先查询北京天气
 * 2. Action: 调用 getWeather("北京")
 * 3. Observation: 北京温度27度
 * 4. Thought: 温度27度超过25度，需要计算差值
 * 5. Action: 调用 subtract(27, 20)
 * 6. Observation: 结果是7
 * 7. Answer: 北京温度27度，比20度高7度
 */
@Slf4j
@Component
public class ReactAgent {

    private final ChatLanguageModel chatModel;
    private static final int MAX_ITERATIONS = 5;  // 防止无限循环

    public ReactAgent(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 执行任务
     *
     * @param task 任务描述
     * @return 执行结果
     */
    public String execute(String task) {
        log.info("🤖 Agent开始执行任务: {}", task);

        List<ChatMessage> messages = new ArrayList<>();

        // 系统提示词：定义Agent行为
        String systemPrompt = """
                你是一个智能Agent，可以使用工具来完成任务。
                
                遵循以下格式思考和行动：
                
                Thought: 分析当前需要做什么
                Action: 决定使用哪个工具，格式为 tool_name(参数)
                Observation: [系统会填充工具返回结果]
                ... (重复 Thought/Action/Observation)
                Thought: 我现在知道最终答案了
                Answer: [最终答案]
                
                可用工具：
                - getWeather(city): 查询天气
                - calculate(expression): 计算数学表达式
                - getCurrentTime(): 获取当前时间
                """;

        messages.add(UserMessage.from(systemPrompt + "\n\n任务：" + task));

        // ReAct循环
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            log.info("📍 Iteration {}/{}", i + 1, MAX_ITERATIONS);

            // 获取模型响应
            Response<AiMessage> response = chatModel.generate(messages);
            AiMessage aiMessage = response.content();
            String text = aiMessage.text();

            log.info("💭 Agent思考: {}", text);
            messages.add(aiMessage);

            // 检查是否完成
            if (text.contains("Answer:")) {
                String answer = extractAnswer(text);
                log.info("✅ 任务完成: {}", answer);
                return answer;
            }

            // 解析并执行工具调用
            if (text.contains("Action:")) {
                String action = extractAction(text);
                String observation = executeTool(action);

                log.info("🔧 执行工具: {}", action);
                log.info("👀 观察结果: {}", observation);

                messages.add(UserMessage.from("Observation: " + observation));
            }
        }

        log.warn("⚠️ 达到最大迭代次数，任务未完成");
        return "任务执行超时，请简化任务或增加步骤限制";
    }

    /**
     * 提取最终答案
     */
    private String extractAnswer(String text) {
        int start = text.indexOf("Answer:") + 7;
        return text.substring(start).trim();
    }

    /**
     * 提取要执行的动作
     */
    private String extractAction(String text) {
        int start = text.indexOf("Action:") + 7;
        int end = text.indexOf("\n", start);
        if (end == -1) end = text.length();
        return text.substring(start, end).trim();
    }

    /**
     * 执行工具（简化版，实际应使用真实工具）
     */
    private String executeTool(String action) {
        // TODO: 实际项目中应该使用ToolExecutor
        // 这里仅做演示
        if (action.startsWith("getWeather")) {
            return "北京今天晴，温度27°C";
        } else if (action.startsWith("calculate")) {
            return "计算结果: 42";
        } else if (action.startsWith("getCurrentTime")) {
            return "2024-02-18 15:30:00";
        }
        return "工具执行失败";
    }

    /**
     * 演示：多步骤任务执行
     */
    public static class Example {
        public static void main(String[] args) {
            // 示例任务
            String task1 = "查询北京天气，如果温度超过25度，告诉我比20度高多少";
            String task2 = "现在几点了？然后计算距离下午6点还有多少小时";
            String task3 = "查询上海和北京的天气，对比温差";

            // 使用Agent执行
            // ReactAgent agent = new ReactAgent(chatModel);
            // String result = agent.execute(task1);
            // System.out.println("结果: " + result);
        }
    }
}

/**
 * 使用说明：
 *
 * 1. 基础用法
 * ```java
 * @Autowired
 * private ReactAgent agent;
 *
 * String result = agent.execute("帮我查询北京天气并计算温差");
 * ```
 *
 * 2. 在Controller中使用
 * ```java
 * @PostMapping("/api/agent/execute")
 * public String executeTask(@RequestBody String task) {
 *     return reactAgent.execute(task);
 * }
 * ```
 *
 * 3. 与现有系统集成
 * ```java
 * @Service
 * public class AgentService {
 *     private final ReactAgent agent;
 *     private final ConsultantService consultant;
 *
 *     public String intelligentChat(String message) {
 *         // 判断是否需要Agent
 *         if (isComplexTask(message)) {
 *             return agent.execute(message);
 *         } else {
 *             return consultant.chat(memoryId, message).collectList().block();
 *         }
 *     }
 * }
 * ```
 *
 * 4. 下一步改进方向
 * - 集成真实的ToolExecutor
 * - 添加工具调用日志和监控
 * - 实现工具选择策略
 * - 支持并行工具调用
 * - 添加人类反馈机制
 */

