package org.example.consultantrag.controller;

import org.example.consultantrag.service.ConsultantService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ConsultantService consultantService;

    public ChatController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    // 前端 fetch 用的是 POST，这里必须是 PostMapping
    @PostMapping("/chat")
    public Flux<String> chat(@RequestParam String memoryId, @RequestBody String message) {
        return consultantService.chat(memoryId, message);
    }
}