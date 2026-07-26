package com.product_service.mapper;

import com.product_service.dto.CategoryDto;
import com.product_service.entity.Category;
import org.modelmapper.ModelMapper;

public class CategoryMapper {
    private static final ModelMapper mapper = new ModelMapper();

    public static CategoryDto  convertCategoryToDto(Category category){
        return  mapper.map(category, CategoryDto.class);
    }

}
