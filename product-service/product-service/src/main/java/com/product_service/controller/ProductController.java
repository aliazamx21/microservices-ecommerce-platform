package com.product_service.controller;

import com.product_service.dto.ApiResponse;
import com.product_service.dto.CategoryDto;
import com.product_service.dto.ProductDto;
import com.product_service.service.CategoryService;
import com.product_service.service.ProductService;
import com.product_service.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final CategoryService categoryService;
    private final ProductService productService;
    private final S3Service s3Service;

    public ProductController(CategoryService categoryService, ProductService productService, S3Service s3Service) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.s3Service = s3Service;
    }

    @GetMapping("/list/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories() {
        List<CategoryDto> categoriesDto = categoryService.findAll();
        ApiResponse<List<CategoryDto>> response = new ApiResponse<>();

        if (categoriesDto != null && !categoriesDto.isEmpty()) {
            response.setMessage("All categories data fetched");
            response.setStatus(200);
            response.setData(categoriesDto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.setMessage("No categories data found");
        response.setStatus(404);
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/list/search")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchProducts(
            @RequestParam String keyword
    ) {
        List<ProductDto> productDtos = productService.searchProduct(keyword);
        ApiResponse<List<ProductDto>> response = new ApiResponse<>();

        if (productDtos != null && !productDtos.isEmpty()) {
            response.setMessage("Products fetched successfully");
            response.setStatus(200);
            response.setData(productDtos);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.setMessage("No products found for the given keyword");
        response.setStatus(404);
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProductImage(
            @RequestHeader(value = "X-Logged-In-User", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam("file") MultipartFile[] files,
            @RequestParam("brandId") int brandId
    ){
        if (role == null || !role.contains("ADMIN")) {
            logger.warn("Unauthorized upload attempt by user: {}", username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Only administrators can upload product images.");
        }

        ArrayList<String> imagePaths = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String url = s3Service.uploadImage(file, brandId);
                if (url != null) {
                    imagePaths.add(url);
                }
            }
            logger.info("Admin {} successfully uploaded {} images for brand {}", username, files.length, brandId);
            return ResponseEntity.ok("Uploaded successfully by " + username + ": " + imagePaths);
        } catch (IOException e) {
            logger.error("Failed to upload image to S3: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to upload image: " + e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Upload failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}