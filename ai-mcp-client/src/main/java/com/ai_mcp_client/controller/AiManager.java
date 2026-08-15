package com.ai_mcp_client.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AiManager implements CommandLineRunner {

    private final ChatClient chatClient;

    // Spring Boot automatically injects the ChatClient Builder and the MCP ToolCallbackProvider
    public AiManager(ChatClient.Builder builder, ToolCallbackProvider toolCallbackProvider) {

        // Bind all remote MCP tools (from your sandbox) to this AI Client globally
        this.chatClient = builder
                .defaultTools(toolCallbackProvider)
                .build();
    }

    @Override
    public void run(String... args) {
        System.out.println("=========================================");
        System.out.println("AI: Let me check the e-commerce product database...");

        // The AI Brain takes your prompt and intelligently decides to use the Sandbox MCP Tools
        String prompt = "Find me high-rated electronics or laptops under $1000";

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        System.out.println("AI Response: " + response);
        System.out.println("=========================================");
    }
}