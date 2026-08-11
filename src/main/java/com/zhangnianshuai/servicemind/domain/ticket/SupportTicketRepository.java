package com.zhangnianshuai.servicemind.domain.ticket;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long> {

    @EntityGraph(attributePaths = {
        "customer",
        "order",
        "afterSalesApplication"
    })
    Optional<SupportTicket>
        findByTicketNoIgnoreCase(String ticketNo);

    @EntityGraph(attributePaths = {
        "customer",
        "order",
        "afterSalesApplication"
    })
    List<SupportTicket>
        findByCustomer_CustomerNoIgnoreCaseOrderByOpenedAtDesc(
            String customerNo
        );

    @EntityGraph(attributePaths = {
        "customer",
        "order",
        "afterSalesApplication"
    })
    List<SupportTicket>
        findByStatusInOrderByOpenedAtAsc(
            Collection<TicketStatus> statuses
        );

    @EntityGraph(attributePaths = {
        "customer",
        "order",
        "afterSalesApplication"
    })
    List<SupportTicket>
        findByAssignedToIgnoreCaseAndStatusInOrderByOpenedAtAsc(
            String assignedTo,
            Collection<TicketStatus> statuses
        );

    @Query(
        value = """
            select t
            from SupportTicket t
            join fetch t.customer c
            left join fetch t.order o
            left join fetch t.afterSalesApplication a
            where lower(t.ticketNo) like :ticketNo
            and lower(c.customerNo) like :customerNo
            and coalesce(lower(t.assignedTo), '') like :assignedTo
            and (
                :category is null
                or t.category = :category
            )
            and (
                :priority is null
                or t.priority = :priority
            )
            and (
                :status is null
                or t.status = :status
            )
            """,
        countQuery = """
            select count(t)
            from SupportTicket t
            join t.customer c
            where lower(t.ticketNo) like :ticketNo
            and lower(c.customerNo) like :customerNo
            and coalesce(lower(t.assignedTo), '') like :assignedTo
            and (
                :category is null
                or t.category = :category
            )
            and (
                :priority is null
                or t.priority = :priority
            )
            and (
                :status is null
                or t.status = :status
            )
            """
    )
    Page<SupportTicket> searchTickets(
        @Param("ticketNo")
        String ticketNo,

        @Param("customerNo")
        String customerNo,

        @Param("assignedTo")
        String assignedTo,

        @Param("category")
        TicketCategory category,

        @Param("priority")
        TicketPriority priority,

        @Param("status")
        TicketStatus status,

        Pageable pageable
    );

    boolean existsByTicketNoIgnoreCase(String ticketNo);
}