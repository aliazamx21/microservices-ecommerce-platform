package com.product_service.controller;

import com.product_service.dto.ApiResponse;
import com.product_service.dto.CategoryDto;
import com.product_service.dto.ProductDto;
import com.product_service.service.CategoryService;
import com.product_service.service.ProductService;
import com.product_service.service.S3Service;
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

    private CategoryService categoryService;
    private ProductService productService;
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
        if (categoriesDto != null) {
            response.setMessage("All categories data fetched");
            response.setStatus(200);
            response.setData(categoriesDto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.setMessage("No categories data found");
        response.setStatus(500);
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/list/search")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchProducts(
            @RequestParam String keyword
    ) {
        List<ProductDto> productDtos = productService.searchProduct(keyword);
        ApiResponse<List<ProductDto>> response = new ApiResponse<>();
        if (productDtos != null) {
            response.setMessage("All categories data fetched");
            response.setStatus(200);
            response.setData(productDtos);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.setMessage("No data found");
        response.setStatus(500);
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProductImage(
            @RequestHeader(value = "X-Logged-In-User") String username, // INJECTED BY GATEWAY
            @RequestHeader(value = "X-User-Role") String role,          // INJECTED BY GATEWAY
            @RequestParam("file") MultipartFile[] files,
            @RequestParam("brandId") int brandId
    ){
        // SECURITY WIN: Only allow ADMIN to upload product images to S3 bucket
        if (role == null || !role.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Only administrators can upload product images.");
        }

        ArrayList<String> imagePaths =  new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String url = s3Service.uploadImage(file, brandId);
                if (url!=null){
                    imagePaths.add(url);
                }
            }
            return ResponseEntity.ok("Uploaded successfully by " + username + ": " + imagePaths);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload image: " + e.getMessage());
        }
    }
}