package com.zhangnianshuai.servicemind.domain.ticket;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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

    boolean existsByTicketNoIgnoreCase(String ticketNo);
}