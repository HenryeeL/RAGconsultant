package org.example.consultantrag.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import dev.langchain4j.store.memory.chat.redis.RedisChatMemoryStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

@Configuration
public class CommonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.chat-model.name:qwen3}")
    private String chatModelName;

    @Value("${ollama.embedding-model.name:qwen3-embedding}")
    private String embeddingModelName;

    @Value("${qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${qdrant.port:6334}")
    private int qdrantPort;

    @Value("${qdrant.collection-name:consultant_knowledge}")
    private String collectionName;

    @Value("${qdrant.vector-dimension:4096}")
    private int vectorDimension;

    @Bean
    @Primary
    public ChatLanguageModel ollamaChatModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(chatModelName)
                .logRequests(true)  // 开启请求日志
                .logResponses(true) // 开启响应日志
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public StreamingChatLanguageModel ollamaStreamingChatModel() {
        return OllamaStreamingChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(chatModelName)
                .logRequests(true)  // 开启请求日志
                .logResponses(true) // 开启响应日志
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(embeddingModelName)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        QdrantClient client = new QdrantClient(
                QdrantGrpcClient.newBuilder(qdrantHost, qdrantPort, false).build()
        );

        try {
            if (!client.listCollectionsAsync().get().contains(collectionName)) {
                client.createCollectionAsync(collectionName,
                        Collections.VectorParams.newBuilder()
                                .setDistance(Collections.Distance.Cosine)
                                .setSize(vectorDimension)
                                .build()).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Qdrant Collection 状态异常: " + e.getMessage());
        }

        return QdrantEmbeddingStore.builder()
                .client(client)
                .collectionName(collectionName)
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.5)
                .build();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        RedisChatMemoryStore redisStore = RedisChatMemoryStore.builder()
                .host(redisHost)
                .port(redisPort)
                .build();

        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(redisStore)
                .build();
    }
}