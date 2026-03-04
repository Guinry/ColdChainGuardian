# Spring AI Dependency Setup for ColdChain Guardian

## Dependency Versions

The project uses Spring AI for the AI assistant functionality. As of March 2026, Spring AI is in active development and some versions may not be available in public repositories.

## Recommended Configuration

### Current Configuration (0.8.1)
```xml
<spring-ai.version>0.8.1</spring-ai.version>
```

### Alternative Configuration (if 0.8.1 not available)
If the version 0.8.1 is not available, you can temporarily disable the AI functionality by:

1. Removing or commenting out the Spring AI dependencies in `pom.xml`
2. Disabling the AI-related imports in the code
3. The core application will continue to function normally

### Production Setup
For production with Alibaba Cloud Qwen, you may need to:

1. Use OpenAI-compatible endpoint configuration
2. Configure DashScope API keys
3. Test connectivity before deployment

## Fallback Strategy
If Spring AI dependencies are unavailable:
1. Comment out AI-related code sections
2. Use mock implementations for demonstration
3. Re-integrate when dependencies become available