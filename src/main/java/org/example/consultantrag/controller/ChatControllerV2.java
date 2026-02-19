package org.example.consultantrag.controller;

import org.example.consultantrag.service.ConsultantServiceV2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 修改后的控制器：
 * 1. 返回值从 Flux<String> 改为 ResponseEntity<String>
 * 2. 移除 SSE (text/event-stream) 媒体类型，改回普通的 JSON 或纯文本
 */
@RestController
@RequestMapping("/api/v2")
public class ChatControllerV2 {

    private final ConsultantServiceV2 consultantServiceV2;

    public ChatControllerV2(ConsultantServiceV2 consultantServiceV2) {
        this.consultantServiceV2 = consultantServiceV2;
    }

    /**
     * 同步聊天接口（支持工具调用）
     * * 注意：因为 Ollama + LangChain4j 目前在流式模式下不支持 Tools，
     * 我们这里改为同步阻塞调用。
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam String memoryId, @RequestBody String message) {
        try {
            // 调用 Service，此时 Service 的 chat 方法返回值应为 String
            String response = consultantServiceV2.chat(memoryId, message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 捕获异常并返回错误信息，方便调试
            return ResponseEntity.internalServerError()
                    .body("服务出错: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public String test() {
        return "ConsultantServiceV2 同步模式已启动，现支持 Function Calling！";
    }
}