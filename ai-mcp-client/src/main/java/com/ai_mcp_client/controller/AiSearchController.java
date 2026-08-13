package com.ai_mcp_client.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiSearchController {

    private final ChatClient chatClient;

    public AiSearchController(ChatClient.Builder builder, ToolCallbackProvider toolCallbackProvider) {
        // Bind the remote MCP tools from the sandbox to this ChatClient globally
        this.chatClient = builder
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam String query) {
        return chatClient.prompt()
                .system("""
                        You are a world-class AI Concierge for an e-commerce platform.
                        Your rules:
                        1. ALWAYS use your product search tools to query the store inventory.
                        2. If exact matches are found, present them enthusiastically with features, price, and rating.
                        3. If no exact match is found, NEVER tell the user 'I found nothing' or leave them empty-handed.
                        4. Use any alternative products provided by the tool to suggest closest matches and explain why they fit the user's needs or budget.
                        """)
                .user(query)
                .call()
                .content();
    }
}