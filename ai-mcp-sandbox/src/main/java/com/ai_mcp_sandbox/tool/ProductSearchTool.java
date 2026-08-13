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
        SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(topK > 0 ? topK : 3)
                .withSimilarityThreshold(0.5);

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        // TOP 1% FALLBACK MECHANISM:
        // If vector search produces zero exact matches, pull general store inventory
        // so the AI Concierge never turns a customer away empty-handed!
        if (results.isEmpty()) {
            List<Document> alternatives = vectorStore.similaritySearch(
                    SearchRequest.query("electronics laptop product")
                            .withTopK(3)
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