# Spring AI Configuration for Qwen (通义千问)

## Configuration Guide

For production deployment with Alibaba Cloud Qwen model, you can configure the application as follows:

### Application Properties
```yaml
spring:
  ai:
    openai:
      # Use Qwen's OpenAI-compatible endpoint
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-max # or qwen-plus, qwen-turbo
```

### Or Java Configuration
```java
@Bean
public ChatModel qwenChatModel() {
    return new OpenAiChatModel(
        new OpenAiClient.Builder()
            .withBaseURL("https://dashscope.aliyuncs.com/compatible-mode/v1")
            .withApiKey(System.getenv("DASHSCOPE_API_KEY"))
            .build(),
        new OpenAiChatOptionsBuilder()
            .withModel("qwen-max")
            .build()
    );
}
```

### Required Dependency (for Qwen specifically)
If/when Spring AI adds direct Qwen support, you would replace the openai dependency with:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-qwen-spring-boot-starter</artifactId>
    <version>${spring-ai.version}</version>
</dependency>
```

### Alternative Approach (Current Recommended)
Since Spring AI primarily offers OpenAI-compatible interfaces, you can currently use Qwen through the OpenAI API compatibility layer by configuring the proper base URL and API key.

This approach allows you to use Qwen's capabilities while leveraging Spring AI's robust features for streaming, memory management, and integration patterns that are already implemented in the ColdChain Guardian system.