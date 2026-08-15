package com.ecom.sandbox.tool;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductSearchTool {

    private final VectorStore vectorStore;

    public ProductSearchTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Tool(description = "Search e-commerce products using natural language query, preferences, or budget limits.")
    public List<String> searchProductsVector(String query, int topK) {

        // Updated to the modern Spring AI SearchRequest Builder API
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK > 0 ? topK : 3)
                .similarityThreshold(0.5)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        // TOP 1% FALLBACK MECHANISM:
        // If vector search produces zero exact matches, pull general store inventory
        // so the AI Concierge never turns a customer away empty-handed!
        if (results.isEmpty()) {
            List<Document> alternatives = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query("electronics laptop product")
                            .topK(3)
                            .build()
            );

            if (!alternatives.isEmpty()) {
                List<String> fallbackContent = alternatives.stream()
                        .map(Document::getContent)
                        .collect(Collectors.toList());
                fallbackContent.add(0, "[SYSTEM NOTE FOR AI]: No exact vector match found for query '" + query + "'. The items below are alternative available products. Reframe your response as a friendly concierge offering these as relevant alternatives.");
                return fallbackContent;
            }
            return List.of("No products currently indexed in store inventory.");
        }

        return results.stream()
                .map(Document::getContent)
                .collect(Collectors.toList());
    }
}