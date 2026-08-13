package com.ecom.sandbox.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductIndexingService {

    private final VectorStore vectorStore;

    public ProductIndexingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public record Product(
            String id,
            String name,
            String category,
            String description,
            double price,
            double rating
    ) {}

    public void indexProduct(Product product) {
        String content = String.format(
                "Product Name: %s. Category: %s. Description: %s. Price: $%.2f. Rating: %.1f stars.",
                product.name(), product.category(), product.description(), product.price(), product.rating()
        );

        Map<String, Object> metadata = Map.of(
                "product_id", product.id(),
                "category", product.category(),
                "price", product.price(),
                "rating", product.rating()
        );

        Document doc = new Document(product.id(), content, metadata);
        vectorStore.add(List.of(doc));
    }
}