package com.zhangnianshuai.servicemind.domain.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = {
        "customer",
        "items",
        "items.product"
    })
    Optional<CustomerOrder>
        findByOrderNoIgnoreCase(String orderNo);

    @EntityGraph(attributePaths = "customer")
    List<CustomerOrder>
        findByCustomer_CustomerNoIgnoreCaseOrderByOrderedAtDesc(
            String customerNo
        );

    @Query(
        value = """
            select o
            from CustomerOrder o
            join fetch o.customer c
            where lower(o.orderNo) like :orderNo
            and lower(c.customerNo) like :customerNo
            and (
                :status is null
                or o.status = :status
            )
            """,
        countQuery = """
            select count(o)
            from CustomerOrder o
            join o.customer c
            where lower(o.orderNo) like :orderNo
            and lower(c.customerNo) like :customerNo
            and (
                :status is null
                or o.status = :status
            )
            """
    )
    Page<CustomerOrder> searchOrders(
        @Param("orderNo")
        String orderNo,

        @Param("customerNo")
        String customerNo,

        @Param("status")
        OrderStatus status,

        Pageable pageable
    );

    boolean existsByOrderNoIgnoreCase(
        String orderNo
    );
}