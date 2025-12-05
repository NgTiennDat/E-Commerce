package com.eCommerce.product.repository;

import com.eCommerce.product.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByIdInOrderById(List<Long> Ids);

    Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable);
}
