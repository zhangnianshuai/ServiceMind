package com.zhangnianshuai.servicemind.application.aftersales;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.aftersales.dto.AfterSalesPageResponse;
import com.zhangnianshuai.servicemind.api.aftersales.dto.AfterSalesResponse;
import com.zhangnianshuai.servicemind.api.aftersales.dto.CreateAfterSalesRequest;
import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesApplication;
import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesApplicationRepository;
import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesStatus;
import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesType;
import com.zhangnianshuai.servicemind.domain.order.CustomerOrder;
import com.zhangnianshuai.servicemind.domain.order.CustomerOrderRepository;
import com.zhangnianshuai.servicemind.domain.order.OrderItem;
import com.zhangnianshuai.servicemind.domain.order.OrderStatus;
import com.zhangnianshuai.servicemind.domain.product.Product;

@Service
@Transactional(readOnly = true)
public class AfterSalesApplicationService {

    private static final Set<AfterSalesStatus> ACTIVE_STATUSES =
        Set.of(
            AfterSalesStatus.SUBMITTED,
            AfterSalesStatus.UNDER_REVIEW,
            AfterSalesStatus.APPROVED,
            AfterSalesStatus.PROCESSING
        );

    private final CustomerOrderRepository orderRepository;
    private final AfterSalesApplicationRepository afterSalesRepository;

    public AfterSalesApplicationService(
            CustomerOrderRepository orderRepository,
            AfterSalesApplicationRepository afterSalesRepository) {
        this.orderRepository = orderRepository;
        this.afterSalesRepository = afterSalesRepository;
    }

    @Transactional
    public AfterSalesResponse create(
            CreateAfterSalesRequest request) {
        String orderNo = requireText(
            request.orderNo(),
            "Order number is required."
        );

        CustomerOrder order = orderRepository
            .findByOrderNoIgnoreCase(orderNo)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Order was not found: " + orderNo
            ));

        OrderItem orderItem = order.getItems()
            .stream()
            .filter(item -> Objects.equals(
                item.getId(),
                request.orderItemId()
            ))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The order item does not belong to this order."
            ));

        validateQuantity(
            request.requestedQuantity(),
            orderItem
        );

        validateOrderEligibility(
            order,
            orderItem,
            request.type()
        );

        if (afterSalesRepository
                .existsByOrderItem_IdAndStatusIn(
                    orderItem.getId(),
                    ACTIVE_STATUSES
                )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An active after-sales application already "
                    + "exists for this order item."
            );
        }

        AfterSalesApplication application =
            new AfterSalesApplication(
                generateApplicationNo(),
                order,
                orderItem,
                request.type(),
                request.requestedQuantity(),
                requireText(
                    request.reason(),
                    "After-sales reason is required."
                ),
                normalizeOptional(request.description())
            );

        try {
            AfterSalesApplication saved =
                afterSalesRepository.saveAndFlush(application);

            return toResponse(saved);
        }
        catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An active after-sales application already "
                    + "exists, or the application data conflicts "
                    + "with an existing record.",
                exception
            );
        }
    }

    public AfterSalesResponse findByApplicationNo(
            String applicationNo) {
        return toResponse(findRequired(applicationNo));
    }

    public List<AfterSalesResponse> findByCustomerNo(
            String customerNo) {
        String normalizedCustomerNo = requireText(
            customerNo,
            "Customer number is required."
        );

        return afterSalesRepository
            .findByOrder_Customer_CustomerNoIgnoreCaseOrderBySubmittedAtDesc(
                normalizedCustomerNo
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public AfterSalesPageResponse searchApplications(
            String applicationNo,
            String orderNo,
            String customerNo,
            AfterSalesType type,
            AfterSalesStatus status,
            int page,
            int size) {

        PageRequest pageable =
            PageRequest.of(
                page,
                size,
                Sort.by(
                    Sort.Direction.DESC,
                    "submittedAt"
                )
            );

        Page<AfterSalesResponse> result =
            afterSalesRepository
                .searchApplications(
                    toLikePattern(applicationNo),
                    toLikePattern(orderNo),
                    toLikePattern(customerNo),
                    type,
                    status,
                    pageable
                )
                .map(this::toResponse);

        return AfterSalesPageResponse.from(result);
    }

    @Transactional
    public AfterSalesResponse startReview(
            String applicationNo) {
        return changeState(
            applicationNo,
            AfterSalesApplication::startReview
        );
    }

    @Transactional
    public AfterSalesResponse approve(
            String applicationNo,
            String reviewComment) {
        String normalizedComment =
            normalizeOptional(reviewComment);

        return changeState(
            applicationNo,
            application -> application.approve(
                normalizedComment
            )
        );
    }

    @Transactional
    public AfterSalesResponse reject(
            String applicationNo,
            String reviewComment) {
        String normalizedComment =
            normalizeOptional(reviewComment);

        if (!StringUtils.hasText(normalizedComment)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A rejection comment is required."
            );
        }

        return changeState(
            applicationNo,
            application -> application.reject(
                normalizedComment
            )
        );
    }

    @Transactional
    public AfterSalesResponse startProcessing(
            String applicationNo) {
        return changeState(
            applicationNo,
            AfterSalesApplication::startProcessing
        );
    }

    @Transactional
    public AfterSalesResponse complete(
            String applicationNo) {
        return changeState(
            applicationNo,
            AfterSalesApplication::complete
        );
    }

    @Transactional
    public AfterSalesResponse cancel(
            String applicationNo) {
        return changeState(
            applicationNo,
            AfterSalesApplication::cancel
        );
    }

    private AfterSalesResponse changeState(
            String applicationNo,
            Consumer<AfterSalesApplication> stateChange) {
        AfterSalesApplication application =
            findRequired(applicationNo);

        try {
            stateChange.accept(application);

            AfterSalesApplication saved =
                afterSalesRepository.saveAndFlush(application);

            return toResponse(saved);
        }
        catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                exception
            );
        }
        catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                exception
            );
        }
    }

    private AfterSalesApplication findRequired(
            String applicationNo) {
        String normalizedApplicationNo = requireText(
            applicationNo,
            "Application number is required."
        );

        return afterSalesRepository
            .findByApplicationNoIgnoreCase(
                normalizedApplicationNo
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "After-sales application was not found: "
                    + normalizedApplicationNo
            ));
    }

    private void validateQuantity(
            Integer requestedQuantity,
            OrderItem orderItem) {
        if (requestedQuantity == null
                || requestedQuantity <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Requested quantity must be greater than zero."
            );
        }

        if (requestedQuantity > orderItem.getQuantity()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Requested quantity exceeds purchased quantity."
            );
        }
    }

    private void validateOrderEligibility(
            CustomerOrder order,
            OrderItem orderItem,
            AfterSalesType type) {
        if (type == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "After-sales type is required."
            );
        }

        switch (type) {
            case RETURN, EXCHANGE ->
                validateReturnOrExchangeOrder(order);

            case REPAIR ->
                validateRepairOrder(order, orderItem);
        }
    }

    private void validateReturnOrExchangeOrder(
            CustomerOrder order) {
        if (order.getStatus() != OrderStatus.SHIPPED
                && order.getStatus() != OrderStatus.COMPLETED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Return or exchange can only be requested "
                    + "for shipped or completed orders."
            );
        }
    }

    private void validateRepairOrder(
            CustomerOrder order,
            OrderItem orderItem) {
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Repair can only be requested "
                    + "for completed orders."
            );
        }

        if (order.getCompletedAt() == null) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The order has no completion timestamp."
            );
        }

        Product product = orderItem.getProduct();
        Integer warrantyMonths = product.getWarrantyMonths();

        if (warrantyMonths == null || warrantyMonths <= 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This product does not provide a repair warranty."
            );
        }

        OffsetDateTime warrantyExpiresAt =
            order.getCompletedAt().plusMonths(warrantyMonths);

        if (OffsetDateTime.now().isAfter(warrantyExpiresAt)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The product warranty has expired."
            );
        }
    }

    private AfterSalesResponse toResponse(
            AfterSalesApplication application) {
        CustomerOrder order = application.getOrder();
        OrderItem orderItem = application.getOrderItem();
        Product product = orderItem.getProduct();

        OffsetDateTime warrantyExpiresAt = null;

        if (order.getCompletedAt() != null
                && product.getWarrantyMonths() != null
                && product.getWarrantyMonths() > 0) {
            warrantyExpiresAt = order
                .getCompletedAt()
                .plusMonths(product.getWarrantyMonths());
        }

        return new AfterSalesResponse(
            application.getApplicationNo(),
            order.getOrderNo(),
            order.getCustomer().getCustomerNo(),
            order.getCustomer().getFullName(),
            orderItem.getId(),
            orderItem.getSkuSnapshot(),
            orderItem.getProductNameSnapshot(),
            application.getType(),
            application.getStatus(),
            application.getRequestedQuantity(),
            application.getReason(),
            application.getDescription(),
            application.getReviewComment(),
            application.getSubmittedAt(),
            application.getReviewedAt(),
            application.getCompletedAt(),
            warrantyExpiresAt
        );
    }

    private String generateApplicationNo() {
        String randomPart = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 20)
            .toUpperCase();

        return "AS" + randomPart;
    }

    private String requireText(
            String value,
            String errorMessage) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                errorMessage
            );
        }

        return value.trim();
    }


    private String toLikePattern(String value) {
        if (!StringUtils.hasText(value)) {
            return "%";
        }

        return "%"
            + value.trim()
                .toLowerCase(java.util.Locale.ROOT)
            + "%";
    }
    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}