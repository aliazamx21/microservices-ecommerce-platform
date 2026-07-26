package com.product_service.service;

import com.product_service.dto.ProductDto;

import java.util.List;

public interface ProductService {
    List<ProductDto> searchProduct(String Keyword);
}
