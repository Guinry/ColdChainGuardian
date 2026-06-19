package com.coldchain.guardian.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiModelClient {

    private static final String DEFAULT_BASE_URL = "https://www.micuapi.ai";
    private static final String DEFAULT_MODEL = "deepseek-v4-pro";
    private static final String DEFAULT_PROTOCOL = "chat-completions";
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:149.0) Gecko/20100101 Firefox/149.0";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final String protocol;
    private final String userAgent;
    private final int maxOutputTokens;
    private final Duration requestTimeout;
    private final URI endpointUri;

    public AiModelClient(ObjectMapper objectMapper,
                         @Value("${ccg.ai.openai.api-key:${SPRING_AI_OPENAI_API_KEY:${OPENAI_API_KEY:not-configured}}}") String apiKey,
                         @Value("${ccg.ai.openai.base-url:${SPRING_AI_OPENAI_BASE_URL:https://www.micuapi.ai}}") String baseUrl,
                         @Value("${ccg.ai.openai.model:${SPRING_AI_OPENAI_CHAT_MODEL:deepseek-v4-pro}}") String model,
                         @Value("${ccg.ai.openai.protocol:${SPRING_AI_OPENAI_PROTOCOL:chat-completions}}") String protocol,
                         @Value("${ccg.ai.openai.user-agent:${SPRING_AI_OPENAI_USER_AGENT:Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:149.0) Gecko/20100101 Firefox/149.0}}") String userAgent,
                         @Value("${ccg.ai.openai.timeout-seconds:90}") int timeoutSeconds,
                         @Value("${ccg.ai.openai.max-output-tokens:4096}") int maxOutputTokens) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null || model.isBlank() ? DEFAULT_MODEL : model.trim();
        this.protocol = protocol == null || protocol.isBlank() ? DEFAULT_PROTOCOL : protocol.trim().toLowerCase();
        this.userAgent = userAgent == null || userAgent.isBlank() ? DEFAULT_USER_AGENT : userAgent.trim();
        this.maxOutputTokens = maxOutputTokens;
        this.requestTimeout = Duration.ofSeconds(Math.max(5, timeoutSeconds));
        this.endpointUri = resolveEndpointUri(baseUrl, this.protocol);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(this.requestTimeout)
                .build();
    }

    public String generate(String prompt) {
        if (apiKey.isBlank() || "not-configured".equals(apiKey)) {
            throw new IllegalStateException("模型服务未配置 API Key，请设置 SPRING_AI_OPENAI_API_KEY 或 OPENAI_API_KEY");
        }

        try {
            String body = objectMapper.writeValueAsString(buildRequestBody(prompt));
            HttpRequest request = HttpRequest.newBuilder(endpointUri)
                    .timeout(requestTimeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .header("User-Agent", userAgent)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("模型服务返回 " + response.statusCode() + "：" + extractErrorMessage(response.body()));
            }

            String text = extractResponseText(response.body());
            if (text.isBlank()) {
                throw new IllegalStateException("模型服务返回成功，但未解析到正文，响应结构：" + summarizeResponseShape(response.body()));
            }
            return text;
        } catch (IOException e) {
            throw new IllegalStateException("模型服务请求或响应解析失败：" + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("模型服务调用被中断", e);
        }
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);

        if (isResponsesProtocol()) {
            body.put("input", prompt);
            body.put("max_output_tokens", maxOutputTokens);
            return body;
        }

        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("max_tokens", maxOutputTokens);
        body.put("temperature", 0.2);
        body.put("stream", false);
        return body;
    }

    private URI resolveEndpointUri(String baseUrl, String protocol) {
        String normalized = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl.trim();
        normalized = normalized.replaceAll("/+$", "");

        if (isResponsesProtocol(protocol)) {
            if (normalized.endsWith("/responses")) {
                return URI.create(normalized);
            }
            if (normalized.endsWith("/v1")) {
                return URI.create(normalized + "/responses");
            }
            return URI.create(normalized + "/v1/responses");
        }

        if (normalized.endsWith("/chat/completions")) {
            return URI.create(normalized);
        }
        if (normalized.endsWith("/v1")) {
            return URI.create(normalized + "/chat/completions");
        }
        return URI.create(normalized + "/v1/chat/completions");
    }

    private String extractResponseText(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);

        String outputText = textValue(root.get("output_text"));
        if (!outputText.isBlank()) {
            return outputText;
        }

        StringBuilder builder = new StringBuilder();
        JsonNode output = root.get("output");
        if (output != null && output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.get("content");
                if (content != null && content.isArray()) {
                    appendContentText(builder, content);
                }
            }
        }

        if (builder.length() > 0) {
            return builder.toString();
        }

        JsonNode choices = root.get("choices");
        if (choices != null && choices.isArray() && !choices.isEmpty()) {
            JsonNode message = choices.get(0).path("message");
            String chatText = extractChatMessageContent(message.get("content"));
            if (!chatText.isBlank()) {
                return chatText;
            }
            String reasoningText = textValue(message.get("reasoning_content"));
            if (!reasoningText.isBlank()) {
                return reasoningText;
            }
        }

        return "";
    }

    private String extractChatMessageContent(JsonNode content) {
        if (content == null || content.isMissingNode() || content.isNull()) {
            return "";
        }

        if (content.isTextual()) {
            return content.asText();
        }

        if (content.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode part : content) {
                String text = textValue(part.get("text"));
                if (text.isBlank()) {
                    text = textValue(part.get("content"));
                }
                if (!text.isBlank()) {
                    builder.append(text);
                }
            }
            return builder.toString();
        }

        return textValue(content.get("text"));
    }

    private boolean isResponsesProtocol() {
        return isResponsesProtocol(protocol);
    }

    private boolean isResponsesProtocol(String value) {
        return "responses".equalsIgnoreCase(value) || "response".equalsIgnoreCase(value);
    }

    private void appendContentText(StringBuilder builder, JsonNode content) {
        for (JsonNode part : content) {
            String text = textValue(part.get("text"));
            if (text.isBlank()) {
                text = textValue(part.path("text").get("value"));
            }
            if (text.isBlank()) {
                text = textValue(part.get("content"));
            }
            if (!text.isBlank()) {
                builder.append(text);
            }
        }
    }

    private String extractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "空响应";
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = textValue(root.path("error").get("message"));
            String type = textValue(root.path("error").get("type"));
            if (!message.isBlank()) {
                return type.isBlank() ? message : message + "（" + type + "）";
            }
        } catch (Exception ignored) {
            // Fall through to a short raw snippet for non-JSON error pages.
        }
        String compact = responseBody.replaceAll("\\s+", " ").trim();
        return compact.length() > 240 ? compact.substring(0, 240) + "..." : compact;
    }

    private String summarizeResponseShape(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "空响应";
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            StringBuilder builder = new StringBuilder();
            root.fieldNames().forEachRemaining(name -> {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(name);
            });
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray()) {
                builder.append("; choices.size=").append(choices.size());
                if (!choices.isEmpty()) {
                    JsonNode message = choices.get(0).get("message");
                    builder.append("; message.fields=");
                    if (message == null) {
                        builder.append("-");
                    } else {
                        StringBuilder messageFields = new StringBuilder();
                        message.fieldNames().forEachRemaining(name -> {
                            if (messageFields.length() > 0) {
                                messageFields.append("|");
                            }
                            messageFields.append(name);
                        });
                        builder.append(messageFields);
                    }
                }
            }
            return builder.toString();
        } catch (Exception ignored) {
            return "非 JSON 响应，长度 " + responseBody.length();
        }
    }

    private String textValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        if (node.isTextual()) {
            return node.asText();
        }
        return node.toString();
    }
}
