package com.zhangnianshuai.servicemind.domain.aftersales;

import java.time.OffsetDateTime;

import com.zhangnianshuai.servicemind.domain.order.CustomerOrder;
import com.zhangnianshuai.servicemind.domain.order.OrderItem;

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
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "after_sales_applications",
    schema = "servicemind"
)
public class AfterSalesApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 32)
    @Column(
        name = "application_no",
        nullable = false,
        unique = true,
        length = 32
    )
    private String applicationNo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private AfterSalesType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AfterSalesStatus status;

    @NotNull
    @Positive
    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;

    @NotBlank
    @Size(max = 200)
    @Column(name = "reason", nullable = false, length = 200)
    private String reason;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Size(max = 500)
    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected AfterSalesApplication() {
    }

    public AfterSalesApplication(
            String applicationNo,
            CustomerOrder order,
            OrderItem orderItem,
            AfterSalesType type,
            Integer requestedQuantity,
            String reason,
            String description) {
        if (order == null) {
            throw new IllegalArgumentException(
                "Order must not be null."
            );
        }

        if (orderItem == null) {
            throw new IllegalArgumentException(
                "Order item must not be null."
            );
        }

        if (requestedQuantity == null
                || requestedQuantity <= 0) {
            throw new IllegalArgumentException(
                "Requested quantity must be greater than zero."
            );
        }

        if (requestedQuantity > orderItem.getQuantity()) {
            throw new IllegalArgumentException(
                "Requested quantity exceeds purchased quantity."
            );
        }

        this.applicationNo = applicationNo;
        this.order = order;
        this.orderItem = orderItem;
        this.type = type;
        this.requestedQuantity = requestedQuantity;
        this.reason = reason;
        this.description = description;
        this.status = AfterSalesStatus.SUBMITTED;
        this.submittedAt = OffsetDateTime.now();
    }

    public void startReview() {
        requireStatus(AfterSalesStatus.SUBMITTED);
        this.status = AfterSalesStatus.UNDER_REVIEW;
    }

    public void approve(String reviewComment) {
        requireStatus(AfterSalesStatus.UNDER_REVIEW);
        this.status = AfterSalesStatus.APPROVED;
        this.reviewComment = reviewComment;
        this.reviewedAt = OffsetDateTime.now();
    }

    public void reject(String reviewComment) {
        requireStatus(AfterSalesStatus.UNDER_REVIEW);

        if (reviewComment == null
                || reviewComment.isBlank()) {
            throw new IllegalArgumentException(
                "A rejection comment is required."
            );
        }

        this.status = AfterSalesStatus.REJECTED;
        this.reviewComment = reviewComment.trim();
        this.reviewedAt = OffsetDateTime.now();
    }

    public void startProcessing() {
        requireStatus(AfterSalesStatus.APPROVED);
        this.status = AfterSalesStatus.PROCESSING;
    }

    public void complete() {
        requireStatus(AfterSalesStatus.PROCESSING);
        this.status = AfterSalesStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
    }

    public void cancel() {
        if (status != AfterSalesStatus.SUBMITTED
                && status != AfterSalesStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                "Only submitted or reviewing applications "
                    + "can be cancelled."
            );
        }

        this.status = AfterSalesStatus.CANCELLED;
    }

    private void requireStatus(
            AfterSalesStatus expectedStatus) {
        if (status != expectedStatus) {
            throw new IllegalStateException(
                "Expected after-sales status "
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

        if (submittedAt == null) {
            submittedAt = now;
        }

        if (status == null) {
            status = AfterSalesStatus.SUBMITTED;
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

    public String getApplicationNo() {
        return applicationNo;
    }

    public CustomerOrder getOrder() {
        return order;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public AfterSalesType getType() {
        return type;
    }

    public AfterSalesStatus getStatus() {
        return status;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public String getReason() {
        return reason;
    }

    public String getDescription() {
        return description;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
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