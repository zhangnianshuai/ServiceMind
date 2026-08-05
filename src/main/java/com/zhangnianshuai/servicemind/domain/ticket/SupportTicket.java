package com.zhangnianshuai.servicemind.domain.ticket;

import java.time.OffsetDateTime;

import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesApplication;
import com.zhangnianshuai.servicemind.domain.customer.Customer;
import com.zhangnianshuai.servicemind.domain.order.CustomerOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "support_tickets",
    schema = "servicemind"
)
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 32)
    @Column(
        name = "ticket_no",
        nullable = false,
        unique = true,
        length = 32
    )
    private String ticketNo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "after_sales_application_id")
    private AfterSalesApplication afterSalesApplication;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private TicketCategory category;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 16)
    private TicketPriority priority;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TicketStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 32)
    private TicketSource source;

    @NotBlank
    @Size(max = 200)
    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @NotBlank
    @Size(max = 2000)
    @Column(
        name = "description",
        nullable = false,
        length = 2000
    )
    private String description;

    @Size(max = 100)
    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Size(max = 2000)
    @Column(name = "resolution", length = 2000)
    private String resolution;

    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime openedAt;

    @Column(name = "first_responded_at")
    private OffsetDateTime firstRespondedAt;

    @Column(name = "assigned_at")
    private OffsetDateTime assignedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected SupportTicket() {
    }

    public SupportTicket(
            String ticketNo,
            Customer customer,
            CustomerOrder order,
            AfterSalesApplication afterSalesApplication,
            TicketCategory category,
            TicketPriority priority,
            TicketSource source,
            String subject,
            String description) {
        if (customer == null) {
            throw new IllegalArgumentException(
                "Customer must not be null."
            );
        }

        if (ticketNo == null || ticketNo.isBlank()) {
            throw new IllegalArgumentException(
                "Ticket number must not be blank."
            );
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                "Ticket subject must not be blank."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                "Ticket description must not be blank."
            );
        }

        this.ticketNo = ticketNo.trim();
        this.customer = customer;
        this.order = order;
        this.afterSalesApplication = afterSalesApplication;
        this.category = category;
        this.priority = priority;
        this.source = source;
        this.subject = subject.trim();
        this.description = description.trim();
        this.status = TicketStatus.OPEN;
        this.openedAt = OffsetDateTime.now();
    }

    public void assignTo(String assignee) {
        ensureNotClosed();

        if (assignee == null || assignee.isBlank()) {
            throw new IllegalArgumentException(
                "Assignee must not be blank."
            );
        }

        this.assignedTo = assignee.trim();
        this.assignedAt = OffsetDateTime.now();
    }

    public void unassign() {
        ensureNotClosed();
        this.assignedTo = null;
        this.assignedAt = null;
    }

    public void changePriority(TicketPriority priority) {
        ensureNotClosed();

        if (priority == null) {
            throw new IllegalArgumentException(
                "Ticket priority must not be null."
            );
        }

        this.priority = priority;
    }

    public void startProgress() {
        if (status != TicketStatus.OPEN
                && status != TicketStatus.WAITING_CUSTOMER
                && status != TicketStatus.RESOLVED) {
            throw new IllegalStateException(
                "Only open, waiting, or resolved tickets "
                    + "can enter progress."
            );
        }

        this.status = TicketStatus.IN_PROGRESS;

        if (firstRespondedAt == null) {
            firstRespondedAt = OffsetDateTime.now();
        }

        if (resolvedAt != null) {
            resolvedAt = null;
            resolution = null;
        }
    }

    public void waitForCustomer() {
        requireStatus(TicketStatus.IN_PROGRESS);
        this.status = TicketStatus.WAITING_CUSTOMER;
    }

    public void resolve(String resolution) {
        requireStatus(TicketStatus.IN_PROGRESS);

        if (resolution == null || resolution.isBlank()) {
            throw new IllegalArgumentException(
                "Resolution must not be blank."
            );
        }

        this.resolution = resolution.trim();
        this.status = TicketStatus.RESOLVED;
        this.resolvedAt = OffsetDateTime.now();
    }

    public void close() {
        requireStatus(TicketStatus.RESOLVED);
        this.status = TicketStatus.CLOSED;
        this.closedAt = OffsetDateTime.now();
    }

    private void ensureNotClosed() {
        if (status == TicketStatus.CLOSED) {
            throw new IllegalStateException(
                "Closed tickets cannot be modified."
            );
        }
    }

    private void requireStatus(TicketStatus expectedStatus) {
        if (status != expectedStatus) {
            throw new IllegalStateException(
                "Expected ticket status "
                    + expectedStatus
                    + " but current status is "
                    + status
                    + "."
            );
        }
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (openedAt == null) {
            openedAt = now;
        }

        if (status == null) {
            status = TicketStatus.OPEN;
        }

        if (priority == null) {
            priority = TicketPriority.NORMAL;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public Customer getCustomer() {
        return customer;
    }

    public CustomerOrder getOrder() {
        return order;
    }

    public AfterSalesApplication getAfterSalesApplication() {
        return afterSalesApplication;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketSource getSource() {
        return source;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getResolution() {
        return resolution;
    }

    public OffsetDateTime getOpenedAt() {
        return openedAt;
    }

    public OffsetDateTime getFirstRespondedAt() {
        return firstRespondedAt;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public Long getVersion() {
        return version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}