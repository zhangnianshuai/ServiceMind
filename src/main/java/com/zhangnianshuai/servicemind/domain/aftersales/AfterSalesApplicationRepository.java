package com.zhangnianshuai.servicemind.domain.aftersales;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AfterSalesApplicationRepository
        extends JpaRepository<AfterSalesApplication, Long> {

    @EntityGraph(attributePaths = {
        "order",
        "order.customer",
        "orderItem",
        "orderItem.product"
    })
    Optional<AfterSalesApplication>
        findByApplicationNoIgnoreCase(String applicationNo);

    @EntityGraph(attributePaths = {
        "order",
        "order.customer",
        "orderItem",
        "orderItem.product"
    })
    List<AfterSalesApplication>
        findByOrder_OrderNoIgnoreCaseOrderBySubmittedAtDesc(
            String orderNo
        );

    @EntityGraph(attributePaths = {
        "order",
        "order.customer",
        "orderItem",
        "orderItem.product"
    })
    List<AfterSalesApplication>
        findByOrder_Customer_CustomerNoIgnoreCaseOrderBySubmittedAtDesc(
            String customerNo
        );

    boolean existsByOrderItem_IdAndStatusIn(
        Long orderItemId,
        Collection<AfterSalesStatus> statuses
    );

    boolean existsByApplicationNoIgnoreCase(
        String applicationNo
    );
}