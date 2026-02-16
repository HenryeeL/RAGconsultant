package org.example.consultantrag.controller;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    @Autowired
    private ChatLanguageModel model; // 建议用通用接口

    // 显式指定 Get 请求，并给参数设置默认值防止 404/400
    @RequestMapping("/chat")
    public String chat(String message) {
        return model.generate(message);
    }
}