package com.ecom.sandbox.controller;

import com.ecom.sandbox.service.ProductIndexingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sandbox")
public class ProductIndexingController {

    private final ProductIndexingService indexingService;

    public ProductIndexingController(ProductIndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @PostMapping("/index")
    public String indexProduct(@RequestBody ProductIndexingService.Product product) {
        indexingService.indexProduct(product);
        return "Successfully indexed product: " + product.name() + " into Qdrant vector store.";
    }
}