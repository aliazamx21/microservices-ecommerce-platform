package com.product_service.service;

import com.product_service.dto.ProductDto;
import com.product_service.entity.Product;
import com.product_service.mapper.ProductMapper;
import com.product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductDto> searchProduct(String keyword) {
        List<Product> products = productRepository.searchProducts(keyword);

        return products.stream()
                .map(ProductMapper::convertProductToDto)
                .collect(Collectors.toList());
    }
}