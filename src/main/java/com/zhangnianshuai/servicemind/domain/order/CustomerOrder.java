package com.zhangnianshuai.servicemind.domain.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.zhangnianshuai.servicemind.domain.customer.Customer;
import com.zhangnianshuai.servicemind.domain.product.Product;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customer_orders", schema = "servicemind")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 32)
    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrderStatus status;

    @NotNull
    @PositiveOrZero
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "ordered_at", nullable = false)
    private OffsetDateTime orderedAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "shipped_at")
    private OffsetDateTime shippedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(String orderNo, Customer customer) {
        this.orderNo = orderNo;
        this.customer = customer;
        this.status = OrderStatus.CREATED;
        this.totalAmount = BigDecimal.ZERO;
        this.orderedAt = OffsetDateTime.now();
    }

    public OrderItem addItem(Product product, BigDecimal unitPrice, int quantity) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException(
                "Items can only be added to orders in CREATED status."
            );
        }

        OrderItem item = new OrderItem(this, product, unitPrice, quantity);
        items.add(item);
        recalculateTotal();
        return item;
    }

    public void markPaid() {
        requireStatus(OrderStatus.CREATED);
        this.status = OrderStatus.PAID;
        this.paidAt = OffsetDateTime.now();
    }

    public void markShipped() {
        requireStatus(OrderStatus.PAID);
        this.status = OrderStatus.SHIPPED;
        this.shippedAt = OffsetDateTime.now();
    }

    public void complete() {
        requireStatus(OrderStatus.SHIPPED);
        this.status = OrderStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
    }

    public void cancel() {
        if (status != OrderStatus.CREATED && status != OrderStatus.PAID) {
            throw new IllegalStateException(
                "Only CREATED or PAID orders can be cancelled."
            );
        }

        this.status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expectedStatus) {
        if (this.status != expectedStatus) {
            throw new IllegalStateException(
                "Expected order status " + expectedStatus
                    + " but current status is " + status + "."
            );
        }
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getLineAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.orderedAt == null) {
            this.orderedAt = now;
        }

        if (this.status == null) {
            this.status = OrderStatus.CREATED;
        }

        if (this.totalAmount == null) {
            this.totalAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Customer getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OffsetDateTime getOrderedAt() {
        return orderedAt;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public OffsetDateTime getShippedAt() {
        return shippedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public Long getVersion() {
        return version;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}