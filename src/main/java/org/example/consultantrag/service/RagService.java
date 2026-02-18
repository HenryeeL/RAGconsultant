package org.example.consultantrag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    private final int MAX_SEGMENT_SIZE = 500;
    private final int MAX_OVERLAP = 50;

    /**
     * 添加文档：仅通过 Tika 提取文本内容
     */
    public int addDocument(InputStream inputStream, String fileName) {
        String extractedText;

        try {
            logger.info(">>> 正在解析文件: {}", fileName);
            ApacheTikaDocumentParser parser = new ApacheTikaDocumentParser();
            Document document = parser.parse(inputStream);
            extractedText = document.text();

            if (extractedText == null || extractedText.trim().isEmpty()) {
                throw new RuntimeException("文件内容为空或无法提取文本（不支持纯图片/扫描件）");
            }

            logger.info("文本提取成功，总字数: {}", extractedText.length());
        } catch (Exception e) {
            logger.error("解析文件失败: {}", e.getMessage());
            throw new RuntimeException("文件解析失败: " + e.getMessage());
        }

        return processAndStore(Document.from(extractedText));
    }

    /**
     * 分块、向量化并入库
     */
    private int processAndStore(Document document) {
        List<TextSegment> segments = DocumentSplitters.recursive(MAX_SEGMENT_SIZE, MAX_OVERLAP)
                .split(document);

        if (segments.isEmpty()) return 0;

        logger.info(">>> 正在进行向量化 (Embedding)...");
        Response<List<Embedding>> embeddingResponse = embeddingModel.embedAll(segments);

        embeddingStore.addAll(embeddingResponse.content(), segments);
        logger.info("成功添加 {} 个片段到 Qdrant 数据库", segments.size());
        return segments.size();
    }

    /**
     * 语义检索
     */
    public List<TextSegment> searchRelevantDocuments(String query, int maxResults) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(0.5)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        return searchResult.matches().stream().map(match -> match.embedded()).toList();
    }
}