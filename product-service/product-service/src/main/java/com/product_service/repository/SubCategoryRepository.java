package com.product_service.repository;

import com.product_service.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubCategoryRepository extends JpaRepository <SubCategory,Integer> {
}
