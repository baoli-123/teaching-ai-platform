package com.example.teachingai.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.teachingai.dto.ChatRequest;
import com.example.teachingai.dto.ChatResponse;
import com.example.teachingai.entity.KnowledgeChunk;
import com.example.teachingai.mapper.KnowledgeChunkMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RagService {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-z0-9]+|[\\u4e00-\\u9fa5]");
    private static final int TOP_K = 3;
    private static final List<String> DICTIONARY = List.of(
            "hashmap", "二叉搜索树", "快速排序", "动态规划", "三次握手",
            "状态码", "https", "事务", "索引", "join", "tcp", "http"
    );

    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final ObjectMapper objectMapper;

    public ChatResponse answer(ChatRequest request) {
        String question = request.getQuestion() == null ? "" : request.getQuestion().trim();
        if (question.isBlank()) {
            return new ChatResponse("answer", "请先输入你想咨询的问题。", List.of(), "empty question");
        }

        List<KnowledgeChunk> chunks = retrieve(request.getCourseId(), question);
        if (chunks.isEmpty()) {
            return new ChatResponse(
                    "answer",
                    "当前知识库中没有找到足够相关的资料，建议换一种问法，或先查看对应课程的课件。",
                    List.of(),
                    "no context"
            );
        }

        String context = buildContext(chunks);
        String llmAnswer = callLlm(question, context);
        String answer = llmAnswer == null ? buildFallbackAnswer(chunks) : llmAnswer;
        List<String> sources = chunks.stream().map(KnowledgeChunk::getTitle).toList();
        return new ChatResponse("answer", answer, sources, "rag");
    }

    private List<KnowledgeChunk> retrieve(Long courseId, String question) {
        List<KnowledgeChunk> candidates = knowledgeChunkMapper.selectList(
                Wrappers.<KnowledgeChunk>lambdaQuery()
                        .eq(courseId != null, KnowledgeChunk::getCourseId, courseId)
        );
        List<String> queryTokens = tokenize(question);
        return candidates.stream()
                .map(chunk -> Map.entry(chunk, score(chunk, queryTokens)))
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_K)
                .map(Map.Entry::getKey)
                .toList();
    }

    private double score(KnowledgeChunk chunk, List<String> queryTokens) {
        String title = chunk.getTitle() == null ? "" : chunk.getTitle().toLowerCase(Locale.ROOT);
        String content = chunk.getContent() == null ? "" : chunk.getContent().toLowerCase(Locale.ROOT);
        String tags = chunk.getTags() == null ? "" : chunk.getTags().toLowerCase(Locale.ROOT);
        String haystack = title + " " + content + " " + tags;
        double score = 0;
        for (String token : queryTokens) {
            int count = countOccurrences(haystack, token);
            score += count;
            if (title.contains(token)) {
                score += 1.5;
            }
        }
        return score;
    }

    private int countOccurrences(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private List<String> tokenize(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        List<String> tokens = new ArrayList<>();
        for (String term : DICTIONARY) {
            if (normalized.contains(term)) {
                tokens.add(term);
            }
        }
        Matcher matcher = TOKEN_PATTERN.matcher(normalized);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private String buildContext(List<KnowledgeChunk> chunks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunk chunk = chunks.get(i);
            sb.append("[").append(i + 1).append("] ")
                    .append(chunk.getTitle()).append("：")
                    .append(chunk.getContent()).append("\n");
        }
        return sb.toString();
    }

    private String buildFallbackAnswer(List<KnowledgeChunk> chunks) {
        StringBuilder sb = new StringBuilder("根据当前教学知识库，我为你整理了以下信息：\n\n");
        for (KnowledgeChunk chunk : chunks) {
            sb.append("· ").append(chunk.getTitle()).append("：").append(chunk.getContent()).append("\n");
        }
        sb.append("\n建议结合课程课件进一步练习，如果有具体疑问可以继续追问。");
        return sb.toString();
    }

    private String callLlm(String question, String context) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        try {
            String baseUrl = System.getenv().getOrDefault("OPENAI_BASE_URL", "https://api.openai.com/v1");
            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", "请基于以下教学知识库回答学生问题。如果知识库不足，请明确说明。\n\n知识库：\n"
                            + context + "\n\n学生问题：" + question
            );
            String payload = objectMapper.writeValueAsString(
                    Map.of(
                            "model", System.getenv().getOrDefault("OPENAI_MODEL", "gpt-4o-mini"),
                            "messages", List.of(message),
                            "temperature", 0.3
                    )
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("choices").path(0).path("message").path("content").asText();
            }
        } catch (Exception ignored) {
            // Fallback answer is used when the remote LLM call fails.
        }
        return null;
    }
}
