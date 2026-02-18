package org.example.consultantrag.controller;

import dev.langchain4j.data.segment.TextSegment;
import org.example.consultantrag.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private static final Logger logger = LoggerFactory.getLogger(RagController.class);

    @Autowired
    private RagService ragService;

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "文件不能为空");
            return response;
        }

        try {
            String fileName = file.getOriginalFilename();
            logger.info("接收上传文件: {}", fileName);

            int count = ragService.addDocument(file.getInputStream(), fileName);

            response.put("success", true);
            response.put("segmentsAdded", count);
            response.put("message", "处理成功，添加了 " + count + " 个知识片段");
        } catch (Exception e) {
            logger.error("文件上传处理失败", e);
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    @PostMapping("/search")
    public Map<String, Object> searchDocuments(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        int maxResults = request.containsKey("maxResults") ? (int) request.get("maxResults") : 5;

        List<TextSegment> results = ragService.searchRelevantDocuments(query, maxResults);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("results", results.stream().map(TextSegment::text).toList());
        return response;
    }
}