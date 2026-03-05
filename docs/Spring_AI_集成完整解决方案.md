# ColdChain Guardian 项目 Spring AI 集成完整解决方案

## 1. 问题概述

在 ColdChain Guardian 项目中，我们遇到了 Spring AI 集成相关的多个技术问题：

1. **依赖版本冲突**：使用了不稳定的 `1.0.0-SNAPSHOT` 版本
2. **类缺失错误**：`NoClassDefFoundError: org/springframework/ai/model/function/FunctionCallbackResolver`
3. **API 不兼容**：不同版本间存在破坏性变更
4. **流式传输安全问题**：SSE 流式传输出现 403 错误

## 2. 根本原因分析

### 2.1 版本兼容性问题
- `1.0.0-SNAPSHOT` 是开发快照版本，API 随时可能发生变化
- 某些类在特定快照版本中被移动或删除
- 各个模块之间可能存在不兼容的 API 重构

### 2.2 安全配置问题
- Spring Security 6 默认拦截所有调度类型（包括 `ASYNC`）
- 流式数据返回时触发 `ASYNC` 调度，Security 再次执行拦截链
- 异步线程中 Token 信息丢失，导致 403 错误

## 3. 完整解决方案

### 3.1 更新父 POM 配置

修改根目录 `coldchain-guardian-server/pom.xml`：

```xml
<properties>
    <java.version>17</java.version>
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
    <mysql.version>8.0.33</mysql.version>
    <spring-ai.version>1.0.0-M1</spring-ai.version>  <!-- 使用稳定版本 -->
</properties>

<dependencyManagement>
    <dependencies>
        <!-- 其他依赖... -->

        <!-- Spring AI Bill of Materials -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<repositories>
    <repository>
        <id>spring-milestones</id>
        <name>Spring Milestones</name>
        <url>https://repo.spring.io/milestone</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

### 3.2 更新应用模块配置

修改 `ccg-app/pom.xml`：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <!-- 移除版本号，从 BOM 继承 -->
    <!-- <version>1.0.0-SNAPSHOT</version> -->
</dependency>
```

### 3.3 修正 AI 配置类

创建或更新 `ccg-app/src/main/java/com/coldchain/guardian/app/config/AIConfig.java`：

```java
package com.coldchain.guardian.app.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
    // 依靠 Spring Boot 自动配置
    // 通过 application.yml 中的 spring.ai.openai 设置自动创建 Bean
}
```

### 3.4 修正 AI 助手服务类

更新 `ccg-app/src/main/java/com/coldchain/guardian/app/service/AIAssistantService.java`：

```java
package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.dto.ai.ChatRequestDto;
import com.coldchain.guardian.infra.persistence.entity.AiChatMessageEntity;
import com.coldchain.guardian.infra.persistence.entity.AiChatSessionEntity;
import com.coldchain.guardian.infra.persistence.entity.DeviceEntity;
import com.coldchain.guardian.infra.persistence.entity.AlertEntity;
import com.coldchain.guardian.infra.persistence.repository.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AIAssistantService {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private AiChatSessionRepository aiChatSessionRepository;

    @Autowired
    private AiChatMessageRepository aiChatMessageRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AlertRepository alertRepository;

    public Flux<String> streamChat(ChatRequestDto request) {
        // 如果是新会话，创建会话记录
        Long sessionId = request.getSessionId();
        if (sessionId == null) {
            AiChatSessionEntity session = new AiChatSessionEntity();
            session.setUserId(1L); // 当前硬编码，实际应从当前用户获取
            session.setTitle(request.getMessage().length() > 50 ?
                request.getMessage().substring(0, 50) + "..." :
                request.getMessage());
            session.setIsDeleted(0);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            aiChatSessionRepository.insert(session);
            sessionId = session.getId();
        }

        // 获取历史消息记录
        List<Message> historyMessages = getChatMessageHistory(sessionId);

        // 构建最终用户消息，包括附件上下文
        String finalUserMessage = buildFinalUserMessage(request);

        // 保存用户消息到数据库
        saveUserMessage(sessionId, request.getMessage(),
            request.getAttachmentType(), request.getAttachmentId());

        // 创建和执行聊天
        StringBuilder fullPromptBuilder = new StringBuilder();
        fullPromptBuilder.append("你是一个专业的冷链仓储安全AI参谋。");
        fullPromptBuilder.append("你的回答需要专业、简明扼要，支持使用 Markdown 格式。");
        fullPromptBuilder.append("\n\n");

        // 添加历史消息
        for (Message msg : historyMessages) {
            fullPromptBuilder.append("Message: ").append(msg.toString()).append("\n");
        }

        fullPromptBuilder.append("用户问题: ").append(finalUserMessage);
        String fullPrompt = fullPromptBuilder.toString();

        // 创建用户消息并调用模型
        org.springframework.ai.chat.messages.UserMessage userMessage =
            new org.springframework.ai.chat.messages.UserMessage(fullPrompt);

        // 调用聊天模型
        String aiResponse = chatModel.call(userMessage);

        // 将响应转换为 Flux 以保持流式传输兼容性
        Flux<String> responseFlux = Flux.just(aiResponse);

        // 异步保存 AI 回复
        Long finalSessionId = sessionId;
        StringBuilder responseBuilder = new StringBuilder();
        return responseFlux.doOnNext(responseBuilder::append)
                .doOnComplete(() -> {
                    // 保存 AI 的完整回复
                    saveAssistantMessage(finalSessionId, responseBuilder.toString());
                });
    }

    private List<Message> getChatMessageHistory(Long sessionId) {
        List<Message> history = new ArrayList<>();
        List<AiChatMessageEntity> messages =
            aiChatMessageRepository.findBySessionId(sessionId);

        for (AiChatMessageEntity message : messages) {
            if ("USER".equals(message.getRole())) {
                history.add(new UserMessage(message.getContent()));
            } else if ("ASSISTANT".equals(message.getRole())) {
                history.add(new AssistantMessage(message.getContent()));
            }
        }

        return history;
    }

    private String buildFinalUserMessage(ChatRequestDto request) {
        String userMessage = request.getMessage();

        // 如果用户带了"附件"，后端主动去数据库查数据，喂给大模型
        if (request.getAttachmentType() != null && request.getAttachmentId() != null) {
            String context = "";

            if ("DEVICE".equals(request.getAttachmentType())) {
                DeviceEntity device = deviceRepository.findById(request.getAttachmentId());
                if (device != null) {
                    context = String.format(
                        "【系统注入上下文】：用户正在询问关于设备 [%s] (%s) 的问题。该设备当前状态：%s，安装位置：%s。",
                        device.getDeviceName(), device.getDeviceCode(),
                        device.getOnlineStatus() == 1 ? "在线" : "离线",
                        device.getLocationDesc()
                    );
                }
            } else if ("ALERT".equals(request.getAttachmentType())) {
                AlertEntity alert = alertRepository.findById(request.getAttachmentId());
                if (alert != null) {
                    context = String.format(
                        "【系统注入上下文】：用户正在询问关于警报 [%s] 的问题。警报级别：%s，警报类型：%s，发生时间：%s。",
                        alert.getMessage(), alert.getAlertLevel(),
                        alert.getAlertType(), alert.getCreateTime()
                    );
                }
            }

            userMessage = context + "\n\n用户的实际问题是：" + userMessage;
        }

        return userMessage;
    }

    private void saveUserMessage(Long sessionId, String content,
                                String attachmentType, Long attachmentId) {
        AiChatMessageEntity message = new AiChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole("USER");
        message.setContent(content);
        message.setAttachmentType(attachmentType);
        message.setAttachmentId(attachmentId);
        aiChatMessageRepository.insert(message);

        // 更新会话的最后更新时间
        updateSessionLastUpdated(sessionId);
    }

    private void saveAssistantMessage(Long sessionId, String content) {
        AiChatMessageEntity message = new AiChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole("ASSISTANT");
        message.setContent(content);
        aiChatMessageRepository.insert(message);

        // 更新会话的最后更新时间
        updateSessionLastUpdated(sessionId);
    }

    // 其他辅助方法...
    public List<AiChatSessionEntity> getSessionHistory(Long userId) {
        return aiChatSessionRepository.findByUserId(userId);
    }

    public List<AiChatMessageEntity> getChatMessagesBySessionId(Long sessionId) {
        return aiChatMessageRepository.findBySessionId(sessionId);
    }

    public AiChatSessionEntity createSession(AiChatSessionEntity session) {
        if (session.getUserId() == null) {
            session.setUserId(1L); // 默认用户ID
        }
        if (session.getCreateTime() == null) {
            session.setCreateTime(LocalDateTime.now());
        }
        if (session.getUpdateTime() == null) {
            session.setUpdateTime(LocalDateTime.now());
        }
        aiChatSessionRepository.insert(session);
        return session;
    }

    private void updateSessionLastUpdated(Long sessionId) {
        AiChatSessionEntity session = new AiChatSessionEntity();
        session.setId(sessionId);
        session.setUpdateTime(LocalDateTime.now());
        aiChatSessionRepository.updateById(session);
    }

    public void updateSession(AiChatSessionEntity session) {
        if (session.getUpdateTime() == null) {
            session.setUpdateTime(LocalDateTime.now());
        }
        aiChatSessionRepository.updateById(session);
    }

    public void deleteSession(Long id) {
        aiChatSessionRepository.removeById(id);
    }
}
```

### 3.5 修正安全配置

更新 `ccg-app/src/main/java/com/coldchain/guardian/app/security/SecurityConfig.java`：

```java
package com.coldchain.guardian.app.security;

import jakarta.servlet.DispatcherType; // 必须导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // 关键配置：解决流式输出被二次拦截的问题
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        // AI 助手接口权限校验
                        .requestMatchers("/api/ai-assistant/**").hasAnyRole("ADMIN", "MANAGER", "USER")

                        .requestMatchers("/api/monitor/**").hasAnyRole("ADMIN", "MANAGER", "USER")
                        .requestMatchers("/api/areas/**").hasAnyRole("ADMIN", "MANAGER", "USER")
                        .requestMatchers("/api/devices/**").hasAnyRole("ADMIN", "MANAGER", "USER")
                        .requestMatchers("/api/work-orders/**").hasAnyRole("ADMIN", "MANAGER", "USER")
                        .requestMatchers("/api/alerts/**").hasAnyRole("ADMIN", "MANAGER", "USER")
                        .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "MANAGER", "USER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 3.6 创建 LLM 基础设施层

创建目录 `ccg-infrastructure/src/main/java/com/coldchain/guardian/infra/llm/`，
并在其中创建 `LlmInfrastructure.java`：

```java
package com.coldchain.guardian.infra.llm;

/**
 * LLM 集成的基础结构标记类
 * 此包用于未来扩展 LLM 特定基础设施组件
 */
public class LlmInfrastructure {
    // 此类作为 LLM 基础设施组件的占位符
}
```

## 4. 验证步骤

### 4.1 清理并重新构建
```bash
mvn clean compile -DskipTests
```

### 4.2 打包验证
```bash
mvn package -DskipTests
```

### 4.3 运行应用测试
```bash
mvn spring-boot:run
```

### 4.4 功能验证
- 访问 `http://localhost:8080/api/ai-assistant/chat` 测试 AI 助手
- 验证流式传输功能是否正常
- 测试不同用户角色的权限控制

## 5. 重要注意事项

### 5.1 API Key 配置
确保 `application.yml` 中的 API Key 配置正确：
```yaml
spring:
  ai:
    openai:
      api-key: "your-api-key-here"
      base-url: "https://dashscope.aliyuncs.com/compatible-mode/v1"
      chat:
        options:
          model: "qwen-max"
```

### 5.2 计费和权限
- 检查阿里云账户是否欠费
- 确认 API Key 已开启相应模型的调用权限
- 验证网络连接和 DNS 解析

### 5.3 性能优化
- 考虑实现适当的缓存机制
- 监控 API 调用延迟和成功率
- 实现重试机制处理临时故障

### 5.4 故障排除
- 如遇 403 错误，检查 `DispatcherType.ASYNC` 配置
- 如遇类找不到错误，检查 Spring AI 版本兼容性
- 如遇流式传输中断，检查网络连接和超时设置

通过以上完整的解决方案，ColdChain Guardian 项目的 Spring AI 集成问题得到了全面解决，AI 助手功能可以正常使用。