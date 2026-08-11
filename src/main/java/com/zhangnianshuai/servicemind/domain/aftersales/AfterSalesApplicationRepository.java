package com.zhangnianshuai.servicemind.domain.aftersales;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(
        value = """
            select a
            from AfterSalesApplication a
            join fetch a.order o
            join fetch o.customer c
            join fetch a.orderItem oi
            join fetch oi.product p
            where lower(a.applicationNo) like :applicationNo
            and lower(o.orderNo) like :orderNo
            and lower(c.customerNo) like :customerNo
            and (
                :type is null
                or a.type = :type
            )
            and (
                :status is null
                or a.status = :status
            )
            """,
        countQuery = """
            select count(a)
            from AfterSalesApplication a
            join a.order o
            join o.customer c
            where lower(a.applicationNo) like :applicationNo
            and lower(o.orderNo) like :orderNo
            and lower(c.customerNo) like :customerNo
            and (
                :type is null
                or a.type = :type
            )
            and (
                :status is null
                or a.status = :status
            )
            """
    )
    Page<AfterSalesApplication> searchApplications(
        @Param("applicationNo")
        String applicationNo,

        @Param("orderNo")
        String orderNo,

        @Param("customerNo")
        String customerNo,

        @Param("type")
        AfterSalesType type,

        @Param("status")
        AfterSalesStatus status,

        Pageable pageable
    );

    boolean existsByOrderItem_IdAndStatusIn(
        Long orderItemId,
        Collection<AfterSalesStatus> statuses
    );

    boolean existsByApplicationNoIgnoreCase(
        String applicationNo
    );
}