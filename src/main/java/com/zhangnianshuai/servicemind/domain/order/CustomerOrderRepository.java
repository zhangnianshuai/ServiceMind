package com.zhangnianshuai.servicemind.domain.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = {
        "customer",
        "items",
        "items.product"
    })
    Optional<CustomerOrder> findByOrderNoIgnoreCase(String orderNo);

    @EntityGraph(attributePaths = "customer")
    List<CustomerOrder>
        findByCustomer_CustomerNoIgnoreCaseOrderByOrderedAtDesc(
            String customerNo
        );

    boolean existsByOrderNoIgnoreCase(String orderNo);
}