package com.zhangnianshuai.servicemind.application.order;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.order.dto.CreateOrderItemRequest;
import com.zhangnianshuai.servicemind.api.order.dto.CreateOrderRequest;
import com.zhangnianshuai.servicemind.api.order.dto.OrderDetailResponse;
import com.zhangnianshuai.servicemind.domain.customer.Customer;
import com.zhangnianshuai.servicemind.domain.customer.CustomerRepository;
import com.zhangnianshuai.servicemind.domain.order.CustomerOrder;
import com.zhangnianshuai.servicemind.domain.order.CustomerOrderRepository;
import com.zhangnianshuai.servicemind.domain.product.Product;
import com.zhangnianshuai.servicemind.domain.product.ProductRepository;
import com.zhangnianshuai.servicemind.domain.product.ProductStatus;

@Service
@Transactional(readOnly = true)
public class OrderCommandService {

    private final CustomerOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderCommandService(
            CustomerOrderRepository orderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderDetailResponse create(
            CreateOrderRequest request) {
        String customerNo = requireText(
            request.customerNo(),
            "Customer number is required."
        );

        Customer customer = customerRepository
            .findByCustomerNoIgnoreCase(customerNo)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Customer was not found: " + customerNo
            ));

        CustomerOrder order = new CustomerOrder(
            generateOrderNo(),
            customer
        );

        Set<String> uniqueSkus = new HashSet<>();

        for (CreateOrderItemRequest requestedItem
                : request.items()) {
            String sku = requireText(
                requestedItem.sku(),
                "Product SKU is required."
            );

            String normalizedSku =
                sku.toUpperCase(Locale.ROOT);

            if (!uniqueSkus.add(normalizedSku)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate product SKU in order: " + sku
                );
            }

            Integer quantity = requestedItem.quantity();

            if (quantity == null || quantity <= 0) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order item quantity must be greater than zero."
                );
            }

            Product product = productRepository
                .findBySkuIgnoreCase(sku)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product was not found: " + sku
                ));

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product is not active: " + sku
                );
            }

            order.addItem(
                product,
                product.getPrice(),
                quantity
            );
        }

        try {
            CustomerOrder saved =
                orderRepository.saveAndFlush(order);

            return OrderDetailResponse.from(saved);
        }
        catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The order conflicts with an existing record.",
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
        catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                exception
            );
        }
    }

    @Transactional
    public OrderDetailResponse pay(
            String orderNo) {
        return changeState(
            orderNo,
            CustomerOrder::markPaid
        );
    }

    @Transactional
    public OrderDetailResponse ship(
            String orderNo) {
        return changeState(
            orderNo,
            CustomerOrder::markShipped
        );
    }

    @Transactional
    public OrderDetailResponse complete(
            String orderNo) {
        return changeState(
            orderNo,
            CustomerOrder::complete
        );
    }

    @Transactional
    public OrderDetailResponse cancel(
            String orderNo) {
        return changeState(
            orderNo,
            CustomerOrder::cancel
        );
    }

    private OrderDetailResponse changeState(
            String orderNo,
            Consumer<CustomerOrder> stateChange) {
        CustomerOrder order = findRequired(orderNo);

        try {
            stateChange.accept(order);

            CustomerOrder saved =
                orderRepository.saveAndFlush(order);

            return OrderDetailResponse.from(saved);
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

    private CustomerOrder findRequired(
            String orderNo) {
        String normalizedOrderNo = requireText(
            orderNo,
            "Order number is required."
        );

        return orderRepository
            .findByOrderNoIgnoreCase(normalizedOrderNo)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Order was not found: "
                    + normalizedOrderNo
            ));
    }

    private String generateOrderNo() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 20)
                .toUpperCase(Locale.ROOT);

            String orderNo = "ORD" + randomPart;

            if (!orderRepository
                    .existsByOrderNoIgnoreCase(orderNo)) {
                return orderNo;
            }
        }

        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Unable to generate a unique order number."
        );
    }

    private String requireText(
            String value,
            String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                errorMessage
            );
        }

        return value.trim();
    }
}