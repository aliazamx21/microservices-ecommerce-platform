package com.product_service.mapper;

import com.product_service.dto.CategoryDto;
import com.product_service.dto.ProductDto;
import com.product_service.entity.Category;
import com.product_service.entity.Product;
import org.modelmapper.ModelMapper;

public class ProductMapper {
    private static final ModelMapper mapper = new ModelMapper();

    public static ProductDto convertProductToDto(Product product){
        return  mapper.map(product, ProductDto.class);
    }

}
