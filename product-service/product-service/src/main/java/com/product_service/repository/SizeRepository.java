package com.product_service.repository;

import com.product_service.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SizeRepository extends JpaRepository<Size, Integer> {
}
