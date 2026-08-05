package com.zhangnianshuai.servicemind.domain.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySkuIgnoreCase(String sku);

    List<Product> findByStatusOrderByNameAsc(ProductStatus status);

    List<Product> findByCategoryIgnoreCaseAndStatusOrderByNameAsc(
        String category,
        ProductStatus status
    );

    boolean existsBySkuIgnoreCase(String sku);
}