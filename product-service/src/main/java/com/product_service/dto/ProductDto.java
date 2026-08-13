package com.product_service.dto;

import com.product_service.entity.Brand;
import com.product_service.entity.SubCategory;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProductDto {

    private Integer id;
    private String name;
    private SubCategoryDto subCategory;
    private Set<BrandDto> brands = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SubCategoryDto getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategoryDto subCategory) {
        this.subCategory = subCategory;
    }

    public Set<BrandDto> getBrands() {
        return brands;
    }

    public void setBrands(Set<BrandDto> brands) {
        this.brands = brands;
    }
}