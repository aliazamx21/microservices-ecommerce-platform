package com.product_service.service;

import com.product_service.dto.ProductDto;
import com.product_service.entity.Product;
import com.product_service.mapper.ProductMapper;
import com.product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{

    private ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductDto> searchProduct(String Keyword) {

        List<Product> products = productRepository.searchProducts(Keyword);
        List<ProductDto> productDtos = new ArrayList<>();

        for (Product p:products){
            productDtos.add(ProductMapper.convertProductToDto(p));
        }
        return productDtos;
    }
}
